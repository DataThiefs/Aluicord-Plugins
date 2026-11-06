package com.github.platformspoofer

import android.content.Context
import android.content.res.Resources
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.discord.api.presence.ClientStatus
import com.discord.api.presence.ClientStatuses
import com.discord.models.presence.Presence
import com.discord.models.user.CoreUser
import com.discord.stores.StoreStream
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem
import com.discord.widgets.channels.list.items.ChannelListItemPrivate
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.friends.FriendsListViewModel
import com.discord.widgets.friends.WidgetFriendsListAdapter
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel

/**
 * Diğer kullanıcıların hangi platform(lar)dan bağlı olduğunu, isimlerinin yanında
 * durum rengine boyanmış simgelerle gösterir (Equicord/Vencord PlatformIndicators benzeri).
 */
@AliucordPlugin(requiresRestart = false)
@Suppress("unused")
class PlatformIndicators : Plugin() {
    init {
        settingsTab = SettingsTab(PlatformIndicatorsSettings::class.java).withArgs(this)
    }

    private val usernameTextId by lazy { Utils.getResId("username_text", "id") }
    private val dmNameId by lazy { Utils.getResId("channels_list_item_private_name", "id") }
    private val friendNameId by lazy { Utils.getResId("friends_list_item_name", "id") }

    override fun start(context: Context) = applyPatches()

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    /** Ayarlar değiştiğinde tüm patch'leri yeniden uygular. */
    fun applyPatches() {
        patcher.unpatchAll()
        if (settings.getBool(KEY_CHAT, true)) safe("chat") { patchChat() }
        if (settings.getBool(KEY_MEMBERS, true)) safe("members") { patchMembersList() }
        if (settings.getBool(KEY_DMS, true)) safe("dms") { patchDMs() }
        if (settings.getBool(KEY_FRIENDS, true)) safe("friends") { patchFriends() }
        if (settings.getBool(KEY_PROFILE, true)) safe("profile") { patchProfile() }
    }

    private inline fun safe(name: String, block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            logger.error("'$name' patch'i uygulanamadı", t)
        }
    }

    // region Patches

    private fun patchChat() {
        val clazz = WidgetChatListAdapterItemMessage::class.java
        val itemNameField = clazz.getDeclaredField("itemName").apply { isAccessible = true }
        patcher.patch(
            clazz.getDeclaredMethod("onConfigure", Int::class.javaPrimitiveType, ChatListEntry::class.java),
            Hook { param ->
                val itemName = itemNameField[param.thisObject] as TextView? ?: return@Hook
                val entry = param.args[1] as? MessageEntry ?: return@Hook
                val author = entry.message.author ?: return@Hook
                if (!settings.getBool(KEY_SHOW_SELF, true) && CoreUser(author).id == selfId()) return@Hook
                val presence = presenceOf(CoreUser(author).id) ?: return@Hook
                addIndicators(itemName, presence, SIZE_CHAT)
            },
        )
    }

    private fun patchMembersList() {
        val clazz = ChannelMembersListViewHolderMember::class.java
        patcher.patch(
            clazz.getDeclaredMethod("bind", ChannelMembersListAdapter.Item.Member::class.java, Function0::class.java),
            Hook { param ->
                val presence = (param.args[0] as ChannelMembersListAdapter.Item.Member).presence ?: return@Hook
                val root = (param.thisObject as RecyclerView.ViewHolder).itemView
                val name = root.findViewById<TextView?>(usernameTextId) ?: return@Hook
                addIndicators(name, presence, SIZE_LIST)
            },
        )
    }

    private fun patchDMs() {
        val clazz = WidgetChannelsListAdapter.ItemChannelPrivate::class.java
        patcher.patch(
            clazz.getDeclaredMethod("onConfigure", Int::class.javaPrimitiveType, ChannelListItem::class.java),
            Hook { param ->
                val presence = (param.args[1] as? ChannelListItemPrivate)?.presence ?: return@Hook
                val root = (param.thisObject as RecyclerView.ViewHolder).itemView
                val name = root.findViewById<TextView?>(dmNameId) ?: return@Hook
                addIndicators(name, presence, SIZE_LIST)
            },
        )
    }

    private fun patchFriends() {
        val int = Int::class.javaPrimitiveType
        val item = FriendsListViewModel.Item::class.java

        patcher.patch(
            WidgetFriendsListAdapter.ItemUser::class.java.getDeclaredMethod("onConfigure", int, item),
            Hook { param ->
                val presence = (param.args[1] as? FriendsListViewModel.Item.Friend)?.presence ?: return@Hook
                val root = (param.thisObject as RecyclerView.ViewHolder).itemView
                val name = root.findViewById<TextView?>(friendNameId) ?: return@Hook
                addIndicators(name, presence, SIZE_LIST)
            },
        )

        patcher.patch(
            WidgetFriendsListAdapter.ItemPendingUser::class.java.getDeclaredMethod("onConfigure", int, item),
            Hook { param ->
                val presence = (param.args[1] as? FriendsListViewModel.Item.PendingFriendRequest)?.presence ?: return@Hook
                val root = (param.thisObject as RecyclerView.ViewHolder).itemView
                val name = root.findViewById<TextView?>(friendNameId) ?: return@Hook
                addIndicators(name, presence, SIZE_LIST)
            },
        )
    }

    private fun patchProfile() {
        patcher.patch(
            UserProfileHeaderView::class.java.getDeclaredMethod(
                "configurePrimaryName",
                UserProfileHeaderViewModel.ViewState.Loaded::class.java,
            ),
            Hook { param ->
                val presence = (param.args[0] as UserProfileHeaderViewModel.ViewState.Loaded).presence ?: return@Hook
                val name = (param.thisObject as View).findViewById<TextView?>(usernameTextId) ?: return@Hook
                addIndicators(name, presence, SIZE_PROFILE)
            },
        )
    }

    // endregion

    // region Helpers

    private fun selfId(): Long = try {
        StoreStream.getUsers().me.id
    } catch (e: Throwable) {
        -1L
    }

    private fun presenceOf(userId: Long): Presence? = try {
        StoreStream.getPresences().presences[userId] as Presence?
    } catch (e: Throwable) {
        null
    }

    private fun addIndicators(view: TextView, presence: Presence, sizeDp: Int) {
        val statuses: ClientStatuses = presence.clientStatuses ?: return
        // ClientStatuses obfuscated getter'ları: a() = desktop, b() = mobile, c() = web
        val entries = arrayOf(
            PlatformType.DESKTOP to statuses.a(),
            PlatformType.MOBILE to statuses.b(),
            PlatformType.WEB to statuses.c(),
        )

        val size = dp(sizeDp)
        val builder = SpannableStringBuilder()
        for ((type, status) in entries) {
            val color = colorFor(status) ?: continue
            val icon = PlatformIconDrawable(type, color).apply { setBounds(0, 0, size, size) }
            builder.append(" ")
            val start = builder.length
            builder.append("\u200B")
            builder.setSpan(ImageSpan(icon, ImageSpan.ALIGN_BOTTOM), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (builder.isNotEmpty()) view.append(builder)
    }

    /** Duruma göre renk; çevrimdışı/görünmez platformlar için null (simge gösterilmez). */
    private fun colorFor(status: ClientStatus?): Int? = when (status) {
        ClientStatus.ONLINE -> settings.getInt(KEY_COLOR_ONLINE, DEFAULT_ONLINE)
        ClientStatus.IDLE -> settings.getInt(KEY_COLOR_IDLE, DEFAULT_IDLE)
        ClientStatus.DND -> settings.getInt(KEY_COLOR_DND, DEFAULT_DND)
        else -> null
    }

    private fun dp(value: Int): Int = (value * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    // endregion

    companion object {
        const val KEY_CHAT = "showInChat"
        const val KEY_MEMBERS = "showInMembers"
        const val KEY_DMS = "showInDMs"
        const val KEY_FRIENDS = "showInFriends"
        const val KEY_PROFILE = "showInProfile"
        const val KEY_SHOW_SELF = "showSelf"

        const val KEY_COLOR_ONLINE = "colorOnline"
        const val KEY_COLOR_IDLE = "colorIdle"
        const val KEY_COLOR_DND = "colorDnd"

        // Discord'un durum renkleri
        val DEFAULT_ONLINE = 0xFF23A55A.toInt()
        val DEFAULT_IDLE = 0xFFF0B232.toInt()
        val DEFAULT_DND = 0xFFF23F43.toInt()

        private const val SIZE_CHAT = 16
        private const val SIZE_LIST = 16
        private const val SIZE_PROFILE = 20
    }
}

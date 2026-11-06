package com.github.platformspoofer

import android.view.View
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

class PlatformIndicatorsSettings(private val plugin: PlatformIndicators) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("PlatformIndicators")

        addSwitch(view.context, PlatformIndicators.KEY_CHAT, "Sohbet", "Mesajlarda kullanıcı adının yanında göster")
        addSwitch(view.context, PlatformIndicators.KEY_MEMBERS, "Üye listesi", "Sunucu üye listesinde göster")
        addSwitch(view.context, PlatformIndicators.KEY_DMS, "DM listesi", "Özel mesaj listesinde göster")
        addSwitch(view.context, PlatformIndicators.KEY_FRIENDS, "Arkadaş listesi", "Arkadaşlar ve bekleyen istekler")
        addSwitch(view.context, PlatformIndicators.KEY_PROFILE, "Profil", "Kullanıcı profilinde isim yanında göster")
        addSwitch(view.context, PlatformIndicators.KEY_SHOW_SELF, "Kendimi göster", "Kendi mesajlarınızda da simgeleri göster")
    }

    private fun addSwitch(context: android.content.Context, key: String, label: String, subtext: String) {
        linearLayout.addView(
            Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, label, subtext).apply {
                isChecked = plugin.settings.getBool(key, true)
                setOnCheckedListener {
                    plugin.settings.setBool(key, it)
                    plugin.applyPatches()
                }
            },
        )
    }
}

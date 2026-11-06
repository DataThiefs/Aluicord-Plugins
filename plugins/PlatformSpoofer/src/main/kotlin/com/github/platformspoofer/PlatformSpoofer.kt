package com.github.platformspoofer

import android.content.Context
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.PreHook
import java.lang.reflect.Member
import java.lang.reflect.Modifier

/**
 * Discord gateway'ine bağlanırken gönderilen IDENTIFY `properties` alanını (os / browser / device)
 * seçilen platforma göre değiştirir. Böylece diğer kullanıcılar sizi seçtiğiniz platformda görür.
 *
 * Discord'un iç sınıfları obfuscate edilmiş olabileceği için birden fazla hook noktası denenir.
 * Hangisi tutarsa o kullanılır; tutmayanlar sadece log'a yazılır, uygulamayı çökertmez.
 */
@AliucordPlugin(requiresRestart = true)
@Suppress("unused")
class PlatformSpoofer : Plugin() {
    init {
        settingsTab = SettingsTab(PlatformSpooferSettings::class.java).withArgs(this)
    }

    val currentPlatform: Platform
        get() = Platform.fromKey(settings.getString(KEY_PLATFORM, Platform.DEFAULT.key))

    fun setPlatform(platform: Platform) = settings.setString(KEY_PLATFORM, platform.key)

    override fun start(context: Context) {
        var hooked = 0

        // 1) Süper özellikleri üreten sınıf: Map döndüren tüm parametresiz metotların sonucunu değiştir.
        hooked += hookMapReturningMethods(ANALYTICS_SUPER_PROPERTIES)

        // 2) Gateway tarafı: properties map'ini parametre olarak alan constructor/metotları değiştir.
        for (className in GATEWAY_CLASSES) hooked += hookMapParameters(className)

        if (hooked == 0) {
            logger.error("Hiçbir hook noktası bulunamadı, platform taklidi çalışmayacak.", null)
            Utils.showToast("PlatformSpoofer: hook noktası bulunamadı (log'a bakın)", true)
        } else {
            logger.info("PlatformSpoofer aktif: ${currentPlatform.label} ($hooked hook)")
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    /** Orijinal map'in `browser`/`os`/`device` alanları değiştirilmiş kopyasını döndürür. İlgisiz map'ler için null. */
    private fun spoof(original: Map<*, *>): Map<String, Any?>? {
        val prefix = when {
            original.containsKey("browser") -> ""
            original.containsKey("\$browser") -> "$"
            else -> return null
        }
        val platform = currentPlatform
        val result = LinkedHashMap<String, Any?>(original.size + 3)
        for ((k, v) in original) result[k.toString()] = v
        result[prefix + "os"] = platform.os
        result[prefix + "browser"] = platform.browser
        result[prefix + "device"] = platform.device
        return result
    }

    private fun hookMapReturningMethods(className: String): Int {
        val clazz = findClass(className) ?: return 0
        var count = 0
        for (method in clazz.declaredMethods) {
            if (method.parameterTypes.isNotEmpty() || !Map::class.java.isAssignableFrom(method.returnType)) continue
            if (Modifier.isAbstract(method.modifiers)) continue
            count += tryPatch(method, Hook { param ->
                val result = param.result as? Map<*, *> ?: return@Hook
                spoof(result)?.let { param.result = it }
            })
        }
        return count
    }

    private fun hookMapParameters(className: String): Int {
        val clazz = findClass(className) ?: return 0
        val members = ArrayList<Member>()
        members.addAll(clazz.declaredConstructors)
        members.addAll(clazz.declaredMethods.filter { !Modifier.isAbstract(it.modifiers) })

        var count = 0
        for (member in members) {
            val params = when (member) {
                is java.lang.reflect.Method -> member.parameterTypes
                is java.lang.reflect.Constructor<*> -> member.parameterTypes
                else -> continue
            }
            if (params.none { Map::class.java.isAssignableFrom(it) }) continue
            count += tryPatch(member, PreHook { param ->
                val args = param.args
                for (i in args.indices) {
                    val arg = args[i] as? Map<*, *> ?: continue
                    spoof(arg)?.let { args[i] = it }
                }
            })
        }
        return count
    }

    private fun tryPatch(member: Member, hook: de.robv.android.xposed.XC_MethodHook): Int = try {
        patcher.patch(member, hook)
        1
    } catch (t: Throwable) {
        logger.error("Patch başarısız: $member", t)
        0
    }

    private fun findClass(name: String): Class<*>? = try {
        Class.forName(name, false, PlatformSpoofer::class.java.classLoader)
    } catch (t: Throwable) {
        logger.info("Sınıf bulunamadı (atlanıyor): $name")
        null
    }

    companion object {
        const val KEY_PLATFORM = "platform"

        private const val ANALYTICS_SUPER_PROPERTIES = "com.discord.utilities.analytics.AnalyticSuperProperties"

        private val GATEWAY_CLASSES = arrayOf(
            "com.discord.gateway.GatewaySocket",
            "com.discord.gateway.io.OutgoingPayload\$Identify",
            "com.discord.gateway.io.Outgoing\$Identify",
        )
    }
}


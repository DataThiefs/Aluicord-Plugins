package com.github.platformspoofer

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

@SuppressLint("SetTextI18n")
class PlatformSpooferSettings(private val plugin: PlatformSpoofer) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("PlatformSpoofer")

        val context = view.context

        linearLayout.addView(
            TextView(context).apply {
                text = "Diğer kullanıcılara hangi platformdan bağlanıyormuş gibi görüneceğinizi seçin.\n" +
                    "Değişikliğin etkili olması için uygulama yeniden başlatılmalıdır."
                val pad = (16 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad / 2)
            },
        )

        val current = plugin.currentPlatform
        val radios = ArrayList<CheckedSetting>()
        var updating = false

        Platform.values().forEach { platform ->
            val radio = Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                platform.label,
                "browser: ${platform.browser} • os: ${platform.os}",
            )
            radio.isChecked = platform == current
            radio.setOnCheckedListener { checked ->
                if (updating || !checked) return@setOnCheckedListener
                updating = true
                radios.forEachIndexed { i, r -> r.isChecked = Platform.values()[i] == platform }
                updating = false

                if (plugin.currentPlatform != platform) {
                    plugin.setPlatform(platform)
                    Utils.promptRestart()
                }
            }
            radios.add(radio)
            linearLayout.addView(radio)
        }
    }
}

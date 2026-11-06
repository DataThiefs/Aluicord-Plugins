package com.github.platformspoofer

/**
 * Gateway IDENTIFY paketindeki `properties` alanına yazılacak değerler.
 * Discord, durum simgesini (masaüstü / mobil / web / konsol / VR) büyük ölçüde `browser` alanına göre belirler.
 */
enum class Platform(
    val key: String,
    val label: String,
    val os: String,
    val browser: String,
    val device: String,
) {
    DESKTOP("desktop", "Windows Desktop Client", "Windows", "Discord Client", ""),
    WEB("web", "Discord Web (Chrome)", "Windows", "Discord Web", ""),
    IOS("ios", "Discord iOS (iPhone)", "iOS", "Discord iOS", "iPhone"),
    ANDROID("android", "Discord Android (Pixel)", "Android", "Discord Android", "Pixel"),
    XBOX("xbox", "Xbox Series X", "Xbox", "Discord Embedded", "Xbox Series X"),
    PLAYSTATION("playstation", "PlayStation 5", "Playstation", "Discord Embedded", "PlayStation 5"),
    VR("vr", "Discord VR Headset", "Android", "Discord VR", "Quest"),
    ;

    companion object {
        val DEFAULT = DESKTOP

        fun fromKey(key: String?): Platform = values().firstOrNull { it.key == key } ?: DEFAULT
    }
}


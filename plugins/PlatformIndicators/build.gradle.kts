version = "1.0.0" // Plugin version. Increment this to trigger an update
description = "Kullanıcıların hangi platformlardan (Masaüstü / Mobil / Web) bağlı olduğunu durum renginde simgelerle gösterir."

aliucord {
    changelog.set(
        """
        # 1.0.0
        * İlk sürüm: sohbet, üye listesi, DM listesi, arkadaş listesi ve profilde platform simgeleri
        * Simgeler duruma göre renklenir: çevrimiçi (yeşil), boşta (sarı), rahatsız etmeyin (kırmızı)
        """.trimIndent(),
    )

    deploy.set(true)
}


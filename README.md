# PlatformSpoofer & PlatformIndicators (Aliucord)

İki Aliucord eklentisi:

| Eklenti | Ne yapar |
|---|---|
| **PlatformSpoofer** | Diğer kullanıcılara seçtiğiniz platformdan bağlanıyormuş gibi görünürsünüz: Desktop, Web, iOS, Android, Xbox, PlayStation, VR |
| **PlatformIndicators** | Diğer kullanıcıların hangi platformdan (🖥️ Masaüstü / 📱 Mobil / 🌐 Web) bağlı olduğunu isimlerinin yanında gösterir. Simgeler duruma göre renklenir: 🟢 çevrimiçi, 🟡 boşta, 🔴 rahatsız etmeyin |

## Kurulum (GitHub üzerinden)

1. Repo: https://github.com/DataThiefs/Aluicord-Plugins (public olmalı).
2. (İsteğe bağlı) `plugins/build.gradle.kts` içindeki `author("DataThiefs", 0L ...)` satırına Discord kullanıcı ID'nizi yazın.
3. Bu klasördeki her şeyi (gizli `.github` klasörü dahil) repoya yükleyin (`main` branch).
4. Repo → **Settings → Actions → General → Workflow permissions** → *Read and write permissions* seçin.
5. **Actions** sekmesinde "Deploy" workflow'u tamamlanınca `builds` branch'inde `.zip` dosyaları oluşur.
6. `.zip` dosyalarını telefona indirin ve **`/sdcard/Aliucord/plugins/`** klasörüne koyun, sonra Aliucord'u yeniden başlatın. Bu yol her zaman çalışır.
   ```
   https://raw.githubusercontent.com/DataThiefs/Aluicord-Plugins/builds/PlatformSpoofer.zip
   https://raw.githubusercontent.com/DataThiefs/Aluicord-Plugins/builds/PlatformIndicators.zip
   ```
   Eklenti otomatik güncellemesi `builds/updater.json` üzerinden çalışır. Yeni sürüm çıkarmak için ilgili eklentinin `build.gradle.kts` dosyasındaki `version` değerini artırın.

## Kullanım

- **PlatformSpoofer:** Ayarlar → Plugins → PlatformSpoofer ⚙️ → platform seçin → uygulamayı yeniden başlatın.
- **PlatformIndicators:** Ayarlardan simgelerin nerede görüneceğini (sohbet, üye listesi, DM, arkadaşlar, profil) seçebilirsiniz.

## Notlar

- Aliucord'un Discord tabanı (v126.21) yalnızca masaüstü/mobil/web durumunu ayrı tutar. Konsol ve VR'dan bağlanan kullanıcılar bu sürümde ayrı simgeyle gösterilemez.
- PlatformSpoofer birden fazla hook noktasını sırayla dener. Çalışmazsa Aliucord → Debug Logs ekranındaki `PlatformSpoofer` satırlarını paylaşın.
- Platform taklidi Discord Kullanım Koşulları'na aykırı sayılabilir; sorumluluk size aittir.

## Lisans
Aliucord plugins-template (public domain) üzerine kurulmuştur.

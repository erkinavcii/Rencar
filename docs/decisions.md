# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dokümantasyondur.

---

### compileSdk ve targetSdk

- Seçim: **37**

- Son Güncelleme Tarihi: 02.07.2026

- Sebep: `androidx.core:core-ktx:1.19.0` ve `androidx.lifecycle:*:2.11.0` bağımlılıkları minimum compileSdk 37 gerektirmektedir. `minSdk` 24'te sabit kalır; yalnızca derleme ve hedef API seviyeleri güncellendi.


### Dependency Injection Kütüphanesi

- Seçim: **Hilt**

- Son Güncelleme Tarihi: 02.07.2026

- Alternatifler: Koin

- Sebep: Compose + ViewModel entegrasyonu için resmi Android desteği; LyraApp deneyimiyle takım tarafından bilinen kütüphane.


### Hilt Annotation Processing

- Seçim: **KSP** (kapt değil)

- Son Güncelleme Tarihi: 02.07.2026

- Sürümler: Hilt **2.59.2**, KSP **2.2.10-2.0.2** (Kotlin 2.2.10 ile birebir uyumlu).

- Compose'da ViewModel: `androidx.hilt:hilt-navigation-compose` (`hiltViewModel()`).

- Sebep: KSP, kapt'a göre belirgin biçimde hızlıdır ve Kotlin 2.2 ile uyumludur.


### AGP 9 Built-in Kotlin + KSP Uyumu

- Karar: `gradle.properties` içinde **`android.disallowKotlinSourceSets=false`** zorunludur.

- Son Güncelleme Tarihi: 02.07.2026

- Sebep: AGP 9 built-in Kotlin kullanır; KSP'nin ürettiği kaynak dizinlerini eklemesi bu bayrak olmadan derlemeyi kırar. Bayrak deneysel (experimental) olarak işaretlidir ancak gereklidir.


### Navigasyon

- Seçim: **Compose Navigation**

- Son Güncelleme Tarihi: 02.07.2026

- Bağımlılık: `androidx.navigation:navigation-compose` (version catalog: `navigationCompose`).

- Uygulama: Tek `NavHost` (`ui/navigation/RencarNavHost.kt`) tüm grafı barındırır; başlangıç hedefi `onboarding` rotasıdır. Navigasyon MVI ile uyumlu kurulur: ViewModel'de navigasyon API'si yoktur; navigasyon `Intent → Effect` üzerinden akar, `Route` Effect'i tüketip `NavHost`'tan gelen lambda'ları çağırır.


### Font Sistemi

- Seçim: **Bundled TTF** (`res/font/`) — geçici olarak `FontFamily.Default` placeholder kullanılıyor.

- Son Güncelleme Tarihi: 02.07.2026

- Fontlar: **Sora** (Display/Heading) + **Plus Jakarta Sans** (Body/UI).

- Geçiş Adımı: https://fonts.google.com adresinden Sora ve Plus Jakarta Sans TTF dosyaları indirilip `res/font/` altına eklenmeli; `Type.kt` içindeki `FontFamily.Default` referansları `FontFamily(Font(R.font.sora_extrabold, FontWeight.ExtraBold), ...)` ile değiştirilmeli.

- Neden Google Fonts API kullanılmadı: `ui-text-google-fonts:1.7.2`, `com_google_android_gms_fonts_certs` resource'unu bundlelamıyor; manuel sertifika dosyası gerektiriyor. Sertifika byte dizilerini §2.2 kuralı gereği uydurmak yasak olduğundan TTF yaklaşımına geçildi.


### Tema Renk Sistemi

- Karar: **Dynamic Color devre dışı**; Rencar renk tokenları (`docs/design/00-color-system.md`) birebir `MaterialTheme` `ColorScheme` slotlarına haritalandı.

- Son Güncelleme Tarihi: 02.07.2026

- Sebep: Android 12+ dynamic color, Rencar marka renklerini bozar. Rencar UI tasarımı iki sabit renk şeması (light/dark) üzerine kuruludur.


### Splash / Onboarding Ekranı

- Karar: MVI mimarisi; repository gerektirmez (saf UI navigasyon ekranı).

- Son Güncelleme Tarihi: 02.07.2026

- Pager altyapısı: 3 sayfa gostergesi ile kuruldu; yalnızca 1. sayfa tasarımda mevcut olduğundan (§2.2) diğer sayfalar tasarım geldiğinde eklenir.

- Başlangıç hedefi: `RencarNavHost` içinde `onboarding` rotası.


### Sunum Katmanı Mimarisi

- Seçim: **MVI (Model-View-Intent)**

- Son Güncelleme Tarihi: 02.07.2026

- Kapsam: Her ekran State + Intent + Effect sözleşmesiyle yazılır. Detaylı kurallar ve referans implementasyon (Login) için bkz. [architecture/mvi-overview.md](architecture/mvi-overview.md).

- Sebep: Tek yönlü veri akışı, durumsuz UI, test edilebilirlik.


### Backend Hazır Değilken Veri Katmanı

- Karar: **Stub repository** deseni — Repository interface + `Fake<X>Repository` implementasyonu.

- Son Güncelleme Tarihi: 02.07.2026

- Sebep: Backend REST API sözleşmesi tanımlı değil (`agents.md` §2.2 uydurmak yasak). Gerçek API geldiğinde yalnızca implementasyon ve DI bağlaması değişir; ViewModel/Contract etkilenmez.

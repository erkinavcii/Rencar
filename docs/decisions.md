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


### Onboarding Ekranı

- Karar: MVI mimarisi; repository gerektirmez (saf UI navigasyon ekranı).

- Son Güncelleme Tarihi: 02.07.2026

- Pager altyapısı: 3 sayfa gostergesi ile kuruldu; yalnızca 1. sayfa tasarımda mevcut olduğundan (§2.2) diğer sayfalar tasarım geldiğinde eklenir.


### Splash Ekranı

- Karar: MVI mimarisi (`ui/splash/`); repository gerektirmez (saf UI, zamanlayıcı tabanlı navigasyon ekranı). Önceden onboarding ile aynı kararda birleşik tutuluyordu; ayrı bir route ve ayrı bir ekran olarak ayrıştırıldı.

- Son Güncelleme Tarihi: 04.07.2026

- Başlangıç hedefi: `RencarNavHost` içinde `splash` rotası — uygulama açılışında ilk gösterilen ekran budur; `SplashViewModel` sabit bir süre (3000ms) sonunda `NavigateToOnboarding` Effect'i gönderir, `SplashRoute` bunu tüketip `onboarding` rotasına geçer ve `popUpTo(splash){inclusive=true}` ile splash geri yığından temizlenir.

- Görsel: Onboarding'deki logo bileşeni (`ic_car` ikonlu, `Primary`/`PrimaryLight` gradyanlı yuvarlak kare) aynı şekilde kullanılır; farkı, logonun arkasında iki katmanlı, gecikmeli (`800ms` faz farkı) genişleyip solan halka animasyonu ve logonun kendisinin girişte ölçek+opaklık animasyonuyla belirmesidir. Yeni bağımlılık gerekmez; `androidx.compose.animation`/`animation-core` API'leri `androidx.compose.material3` üzerinden zaten classpath'te mevcuttur.


### Sunum Katmanı Mimarisi

- Seçim: **MVI (Model-View-Intent)**

- Son Güncelleme Tarihi: 02.07.2026

- Kapsam: Her ekran State + Intent + Effect sözleşmesiyle yazılır. Detaylı kurallar ve referans implementasyon (Login) için bkz. [architecture/mvi-overview.md](architecture/mvi-overview.md).

- Sebep: Tek yönlü veri akışı, durumsuz UI, test edilebilirlik.


### Backend Hazır Değilken Veri Katmanı

- Karar: **Stub repository** deseni — Repository interface + `Fake<X>Repository` implementasyonu.

- Son Güncelleme Tarihi: 02.07.2026

- Sebep: Backend REST API sözleşmesi tanımlı değil (`agents.md` §2.2 uydurmak yasak). Gerçek API geldiğinde yalnızca implementasyon ve DI bağlaması değişir; ViewModel/Contract etkilenmez.


### Auth Veri Katmanı (Gercek Repository)

- Karar: `AuthRepository` interface + `AuthRepositoryImpl` (gercek Retrofit cagrilari).

- Son Güncelleme Tarihi: 02.07.2026

- Sebep: `rencar.halitkalayci.com/api` canli; kontratin tanimlı olmadigi stub deseni yerine gercek implementasyon kullanildi. `AuthRepositoryImpl`, basarili `verifyOtp` donus degerinde `TokenManager.saveTokens()` cagirir. `register` cagrisinda token kaydedilmez; kullanici onboarding'e yonlendirilip OTP akisiyla giris yapar.

- DI: `di/AuthModule.kt` — `@Binds @Singleton` ile `AuthRepositoryImpl → AuthRepository`.

- BASE_URL: `https://rencar.halitkalayci.com/` (kök dizin). Swagger dokumanı `/api/docs-json` adresinde olsa da API rotalari `/api/` prefix'i olmadan kök seviyesinde calisir. `/api/health` 404, `/health` 200 donerek dogrulandi.


### Auth Ekranlari Akisi

- Karar: Giris = telefon + OTP, Kayit = form + onboarding'e donus.

- Son Güncelleme Tarihi: 02.07.2026

- Akis:
  - Onboarding "Hemen Basla" → Register → `POST /auth/register` → basari snackbar → Onboarding'e pop.
  - Onboarding "Giris Yap" → Login → `POST /auth/login` (phone) → OTP → `POST /auth/verify-otp` (phone + 6 hane) → Home (TODO).

- OTP telefon iletimleri: Nav arg olarak 10 haneli lokal numara gecilir (ornek: "5320000000"). ViewModel'de API cagrisinda "+90" oneklenir.

- OTP geri sayim: `OtpViewModel` icerisinde 60 saniyeye sabit; `expiresAt` alani parse edilmez.

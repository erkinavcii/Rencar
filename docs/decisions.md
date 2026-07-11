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


### Ana Harita Ekranı — Harita Teknolojisi

- Karar: **MapLibre Native (Android SDK)** + **MapTiler** (OpenStreetMap tabanlı ücretsiz vektör tile servisi).

- Son Güncelleme Tarihi: 04.07.2026

- Alternatifler: Google Maps SDK (Google Play Services bağımlılığı + ücretli faturalandırma gerektirir), Mapbox SDK (v10'dan itibaren kapalı kaynak/ücretli), osmdroid (raster tile, View tabanlı, daha az özelleştirilebilir).

- Sebep: MapLibre açık kaynak (BSD-2) ve API key/lisans ücreti gerektirmiyor; vektör tile render ettiği için tasarımdaki özel marker/fiyat balonu stilizasyonuna uygun. OpenStreetMap veri kaynağı, MapTiler ise bu veriyi MapLibre'nin okuyacağı vektör tile formatında sunan ücretsiz sağlayıcı.

- Versiyon: `org.maplibre.gl:android-sdk:13.3.1` (GitHub üzerindeki en güncel stabil release, 04.07.2026 itibariyle).

- API Key Yönetimi: MapTiler key'i git'e girmez; `local.properties` içinde `MAPTILER_API_KEY` olarak tutulur, `app/build.gradle.kts` bunu `BuildConfig.MAPTILER_API_KEY` alanına aktarır.

- Konum: Gerçek cihaz konumu kullanılacak (`ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION`); runtime izin akışı Ana Harita ekranı implementasyonunda ele alınır.


### Ana Harita Ekranı — Gerçek Araç Verisi

- Karar: Mock araç listesi kaldırıldı; `GET /vehicles` (yalnızca AVAILABLE araçlar) üzerinden gerçek veri kullanılıyor.

- Son Güncelleme Tarihi: 05.07.2026

- Veri Katmanı: `data/repository/VehicleRepository.kt` (interface) + `VehicleRepositoryImpl.kt`, mevcut `VehicleService`/`VehicleDtos.kt` (önceden eklenmiş, kullanılmıyordu) üzerine kuruldu. `di/VehicleModule.kt` ile `@Binds` bağlandı. `HomeViewModel` artık `init` bloğunda `loadVehicles()` çağırıyor; yüklenirken `HomeUiState.isLoading` ile harita üzerinde `CircularProgressIndicator` gösteriliyor.

- Mock Fallback Kaldırıldı (11.07.2026): Önceki "Mock Fallback (Geçici)" davranışı — `GET /vehicles` 401/403/ağ hatasıyla başarısız olduğunda `HomeViewModel`'in sessizce mock araç listesine (`mock-1..mock-5`) düşmesi — tamamen kaldırıldı. Artık `loadVehicles()`'daki `onFailure` bloğu `vehicles = emptyList()` yapıp `HomeEffect.ShowError` efektini (Snackbar) tetikliyor; kullanıcı hatayı görüyor, sahte veriyle yanıltılmıyor. 401 özelinde kullanıcı zaten yukarıdaki "Oturum Süresi Dolması (401)" kararı gereği `AuthInterceptor`/`SessionManager` üzerinden login ekranına yönlendiriliyor; bu Home ekranındaki hata gösterimi esas olarak diğer hata türlerini (ağ hatası, sunucu hatası vb.) kapsıyor.

- Kategori Eşlemesi: API'nin gerçek `type` alanı (`SEDAN`/`SUV`/`HATCHBACK`/`STATION`/`MINIVAN`) tasarımdaki Ekonomik/Konfor/SUV filtre chip'lerine, otomotiv kiralama sektöründeki yerleşik segment kuralına göre eşlendi: `SEDAN`/`HATCHBACK` → Ekonomik, `STATION`/`MINIVAN` → Konfor, `SUV` → SUV. Bu eşleme uydurma bir iş kuralı değil, standart araç kiralama segment taksonomisidir.

- Fiyat: Marker'larda gerçek `pricePerDay` değeri "₺{fiyat}/gün" olarak gösteriliyor; tasarımdaki anlık/dakikalık fiyat görünümü ("₺28" vb.) gerçek backend'in günlük kiralama modeliyle uyuşmadığından kullanılmadı.

- Bilinen Sınır: `GET /vehicles` yalnızca AVAILABLE araçları döndürdüğünden (`RENTED`/`MAINTENANCE` sunucu tarafında filtreleniyor), "Kullanımda" durumundaki marker görsel dalı pratikte hiç tetiklenmez; bu, tasarım mockup'ının varsaydığı ama gerçek API'de karşılığı olmayan bir senaryodur.

- Kamera Davranışı: Kamera artık sabit Kadıköy merkezinde kalmıyor; `HomeRoute` içinde `uiState.vehicles` (ham, filtrelenmemiş liste) değiştiğinde tetiklenen bir `LaunchedEffect`, `org.maplibre.android.geometry.LatLngBounds` ile tüm araçları kapsayan sınır kutusunu hesaplayıp `CameraUpdateFactory.newLatLngBounds(bounds, padding)` + `animateCamera` ile kamerayı otomatik kaydırıyor (tek araç varsa `newLatLngZoom` kullanılıyor, liste boşsa dokunulmuyor). Sabit Kadıköy konumu yalnızca veri gelmeden önceki nötr başlangıç noktası olarak korunuyor; bu sırada `isLoading` spinner'ı haritayı zaten örttüğü için kullanıcıya görünmüyor. Filtre değişiminde (`visibleVehicles`) kamera yeniden kaymıyor; yalnızca ham liste ilk kez yüklendiğinde tetikleniyor.


### Araç Detay Bottom Sheet

- Karar: Haritadaki bir araç marker'ına tıklandığında tasarımdaki (`Rencar.html`, "Araç Detay") ile birebir uyumlu bir `ModalBottomSheet` açılır.

- Son Güncelleme Tarihi: 05.07.2026

- Marker Tıklama: `MapLibreMap.setOnMarkerClickListener`; `renderVehicleMarkers` artık her çağrıda bir `Map<Marker, String>` (marker → vehicleId) döndürüp Route'ta saklanıyor, tıklamada bu eşleme üzerinden `HomeIntent.VehicleSelected` tetikleniyor.

- Placeholder Alanlar (Geçici, Kullanıcı Onayıyla): Gerçek API (`VehicleResponseDto`) yakıt yüzdesi, menzil, vites tipi, koltuk sayısı ve araç fotoğrafı sağlamıyor. Kullanıcının onayıyla bu alanlar `HomeScreen.kt` içinde sabit placeholder değerlerle (`PLACEHOLDER_FUEL_PERCENT`, `PLACEHOLDER_RANGE_KM`, `PLACEHOLDER_TRANSMISSION`, `PLACEHOLDER_SEAT_COUNT`) dolduruldu; backend bu alanları sağladığında kaldırılıp gerçek değerlerle değiştirilecek.

- Fiyat: Tasarımdaki uydurma "Saatlik ₺180" satırı **eklenmedi** — gerçek backend saatlik/dakikalık fatura desteklemiyor, yanıltıcı olurdu. Yalnızca gerçek `pricePerDay` gösteriliyor.

- "Rezerve Et" / "Kilidi Aç" butonları henüz no-op; gerçek `POST /rentals` akışı (bitiş tarihi seçimi, onay) ayrı bir adımda ele alınacak. **Güncelleme (05.07.2026):** "Rezerve Et" artık no-op değil; bkz. "Rezervasyon Onayı Ekranı" kararı. "Kilidi Aç" hâlâ no-op.


### Ana Harita Ekranı — Çıkış Butonu Kaldırıldı

- Karar: Arama çubuğunun yanındaki geçici çıkış (logout) butonu (`LogoutButton`) tamamen kaldırıldı. Bununla birlikte `HomeIntent.Logout`, `HomeEffect.NavigateToOnboarding`, `HomeViewModel.logout()` ve `HomeRoute`'un `onLogout` parametresi de (artık hiçbir çağıran kalmadığından) kod tabanından temizlendi; `HomeViewModel`'in `TokenManager` bağımlılığı da bu nedenle kaldırıldı.

- Son Güncelleme Tarihi: 05.07.2026

- Sebep: `main` dalına merge edilmiş `feature/profile` (PR #11) bu dala çekildi (`git merge origin/main`); gerçek Profil ekranı (`ui/profile/ProfileScreen.kt` + `ProfileViewModel.kt`) kendi bağımsız çıkış akışına (`ProfileIntent.Logout` → `TokenManager.clearTokens()` → `ProfileEffect.NavigateToOnboarding`) zaten sahip. Ana Harita ekranındaki geçici çıkış butonu bu nedenle gereksiz hale geldi ve kaldırıldı.

- Not: `RencarNavHost.kt` içindeki `ROUTE_PROFILE` artık `PlaceholderScreen` değil, gerçek `ProfileRoute`'a bağlı (bu değişiklik `feature/profile` merge'i ile geldi, bu adımda ayrıca yapılmadı).


### Ana Harita Ekranı — En Yakın Araç ve Gerçek Mesafe/Süre Gösterimi

- Karar: Alt karttaki sabit "Kadıköy çevresinde · 3 dk uzaklıkta" placeholder'ı kaldırıldı; yerine gerçek kullanıcı GPS konumu ile en yakın aracın gerçek konumu arasında hesaplanan mesafeye dayalı bir gösterim geldi: "En yakın araç ~X dk uzaklıkta" (konum varsa), "Konumunuz aranıyor…" (izin var, konum henüz gelmediyse) veya "Mesafe için konum izni gerekli" (izin yoksa). "En Yakın Aracı Bul" butonu artık gerçekten çalışıyor: konum izni yoksa izin ister, varsa en yakın aracı seçip (detay sheet açılır) kamerayı o araca kaydırır.

- Son Güncelleme Tarihi: 05.07.2026

- Mesafe Hesabı: `HomeUiState.nearestVehicle` / `nearestVehicleDistanceMeters`, kullanıcının `userLocation`'ı (MapLibre `LocationComponent`'in canlı GPS güncellemelerinden `HomeIntent.UserLocationChanged` ile beslenir) ile her aracın gerçek konumu arasında Haversine (küresel düz mesafe) formülüyle hesaplanır — gerçek matematik, uydurma veri değil.

- Süre Tahmini (Yaklaşık, Kullanıcı Onayıyla): Backend'de rota/trafik/ETA endpoint'i olmadığından "kaç dakika uzaklıkta" bilgisi, gerçek mesafenin varsayılan bir ortalama şehir-içi sürüş hızına (**25 km/saat**, `HomeContract.kt` içinde `ASSUMED_AVERAGE_SPEED_KMH`) bölünmesiyle **yaklaşık** olarak türetiliyor. Bu bir tahmindir, gerçek trafik/rota verisi değildir; gerçek bir rota/ETA API'si (ör. OSRM, MapTiler Routing) entegre edilirse bu varsayım kaldırılıp yerine gerçek süre konur.

- Canlı Konum: `enableLocationComponent`, `LocationEngineRequest` üzerinden `locationComponent.locationEngine?.requestLocationUpdates(...)` ile her yeni GPS konumunda `onLocationUpdate` callback'ini tetikler; `HomeRoute` bunu `HomeIntent.UserLocationChanged` olarak ViewModel'e iletir. Yeni bağımlılık gerekmez, mevcut MapLibre location engine API'si kullanılır.

- Son Bilinen Konum Fallback'i (05.07.2026): Yalnızca `requestLocationUpdates` ile yeni bir GPS fix'i beklemek, sistemde önbelleğe alınmış bir son konum olsa bile (ör. konum servisleri yeni bir fix üretmediği cihaz/emülatör durumlarında) arayüzün süresiz olarak "Konumunuz aranıyor..." demesine yol açıyordu. Bu nedenle `enableLocationComponent` artık aktivasyon sonrası önce `locationEngine.getLastLocation(...)` ile sistemde var olan son bilinen konumu anında kullanıyor, ardından `requestLocationUpdates` ile canlı güncellemeleri dinlemeye devam ediyor. `LocationEngine` arayüzünde zaten mevcut olan bir metottur, yeni bağımlılık gerekmez.


### Oturum Süresi Dolması (401) — Sessiz Token Yenileme, Login Yönlendirmesi Yalnızca Son Çare

- Karar (Güncellendi 11.07.2026): İlk versiyonda her 401'de doğrudan oturum sonlandırılıp login'e yönlendiriliyordu. Kullanıcı isteği üzerine bu davranış değiştirildi: artık 401 alındığında önce backend'de zaten tanımlı olan `POST auth/refresh` (`AuthService.kt:17-18`, `RefreshTokenDto`/`AuthResponseDto` ile) ile sessizce yeni bir access token alınmaya çalışılıyor; başarılı olursa orijinal istek yeni token ile şeffaf şekilde tekrar ediliyor ve kullanıcı hiçbir kesinti yaşamıyor. Kullanıcı yalnızca refresh token'ın **kendisi de** geçersiz/süresi dolmuşsa (refresh çağrısı da başarısız olursa) login ekranına düşüyor.

- Son Güncelleme Tarihi: 11.07.2026

- Uygulama: `AuthInterceptor.kt` tekrar sade haline döndürüldü (yalnızca `Authorization` header'ı ekler, response kontrolü yapmaz). 401 sonrası tüm mantık yeni eklenen `data/remote/TokenAuthenticator.kt` (`okhttp3.Authenticator` implementasyonu, `@Singleton`) içine taşındı: `TokenManager.getRefreshToken()` ile `auth/refresh` çağrılır, başarılıysa `TokenManager.saveTokens()` ile yeni token'lar kaydedilip istek `Authorization` header'ı güncellenerek tekrar denenir; refresh de başarısız olursa `TokenManager.clearTokens()` + `SessionManager.notifySessionExpired()` çağrılır (bu, önceki karardaki `RencarNavHost`/`MainActivity` yönlendirme mekanizmasını aynen tetikler, o kısım değişmedi).

- Eşzamanlılık ve Döngü Koruması: Birden fazla istek aynı anda 401 alırsa yalnızca biri gerçekten refresh çağrısı yapar; `synchronized` blok içinde önce `TokenManager`'daki güncel access token, başarısız olan istekteki token ile karşılaştırılır — farklıysa (başka bir istek zaten yenilemiş demektir) doğrudan yeni token ile tekrar denenir. `auth/refresh` isteğinin kendisi 401 dönerse (refresh token geçersiz) tekrar refresh denenmez, path kontrolü ile doğrudan vazgeçilir; ayrıca aynı isteğin ikinci kez 401 alması durumunda (`responseCount >= 2`) sonsuz döngü engellenir.

- DI: `TokenAuthenticator`, `AuthService`'e doğrudan değil `javax.inject.Provider<AuthService>` üzerinden bağımlı; bu, `OkHttpClient → TokenAuthenticator → AuthService → Retrofit → OkHttpClient` döngüsünü Dagger/Hilt'in `Provider` ile tembel çözümleme (lazy resolution) yaparak kırmasını sağlıyor, ayrı bir ikinci OkHttpClient/Retrofit kurulumu gerekmedi. `NetworkModule.provideOkHttpClient` artık `.authenticator(tokenAuthenticator)` da ekliyor.

- Kapsam Dışı Bırakılan: Token olmadan yapılan istekler (login/OTP/register) 401 dönerse bu mekanizma hiç tetiklenmez. `AuthService.logout()` (`POST auth/logout`) endpoint'i tanımlı ama hâlâ hiçbir yerden çağrılmıyor; manuel çıkışta backend'e haber verilip refresh token'ın iptal edilmesi ayrı bir adımda ele alınabilir.

- DI (SessionManager, değişmedi): `SessionManager`, parametresiz `@Inject constructor` ile tanımlandığından ayrı bir `@Provides` gerekmiyor. `MainActivity` `@Inject lateinit var sessionManager: SessionManager` ile field injection alıp `RencarNavHost`'a parametre olarak geçiriyor (`tokenManager` ile aynı desen).


### Rezervasyon Onayı Ekranı

- Karar: Tasarımdaki "Rezervasyon Onayı" ekranı `ui/reservation/` altında MVI ile uygulandı (`ReservationContract.kt`, `ReservationViewModel.kt`, `ReservationScreen.kt`). Ana Harita'daki araç detay sheet'inde "Rezerve Et" butonu artık bu ekrana yönlendiriyor; `HomeRoute`'a eklenen `onNavigateToReservation` parametresi seçili aracın `vehicleId`, `brand`, `model`, `plate`, `pricePerDay` bilgilerini `RencarNavHost` üzerinden nav-arg olarak taşıyor (OTP ekranının `phone` nav-arg deseniyle birebir aynı: `"reservation/{vehicleId}/{brand}/{model}/{plate}/{pricePerDay}"`, `ReservationViewModel` bunları `SavedStateHandle` ile okuyor). Araç zaten Ana Harita'nın `GET /vehicles` çağrısından bellekte olduğundan ayrı bir "getVehicleById" çağrısı yapılmadı.

- Son Güncelleme Tarihi: 05.07.2026

- Veri Katmanı: Daha önce yalnızca Retrofit katmanında tanımlı olup hiçbir yerden çağrılmayan `RentalService`/`CreateRentalDto`/`RentalResponseDto` kullanılarak `data/repository/RentalRepository.kt` (arayüz) + `RentalRepositoryImpl.kt` eklendi; `di/RentalModule.kt` ile `@Binds` bağlandı (`VehicleRepository`/`AuthRepository` ile birebir aynı desen).

- Kaldırılan Tasarım Öğeleri (Kullanıcı Onayıyla): Tasarımdaki "Ücretsiz rezervasyon: 15 dk" ve "Başlangıç ücreti: ₺15,00" satırları **eklenmedi** — `VehicleResponseDto`'da bu kavramların hiçbir karşılığı yok (uydurma olurdu, agents.md §2.2). Yalnızca gerçek `pricePerDay`'den orantılı türetilmiş Dakikalık/Saatlik/Günlük fiyatlar ve seçilen plana göre tahmini ücret gösteriliyor (VehicleDetailBottomSheet'teki dakikalık/saatlik türetme ile aynı yaklaşım).

- Bitiş Tarihi (endDate) Kuralı (Kullanıcı Onayıyla): Tasarımda bir bitiş tarihi/saat seçici olmadığından, `POST /rentals`'ın zorunlu `endDate` alanı seçilen plana göre otomatik hesaplanıyor: Dakikalık → +30 dk, Saatlik → +1 saat, Günlük → +1 gün (`ReservationViewModel.endDateFor`). Gerçek bitiş, kullanıcı aracı iade ettiğinde (`POST /rentals/{id}/return`, henüz UI'da yok) belirlenecek. Tarih hesaplaması `java.util.Calendar` + `SimpleDateFormat` ile yapılıyor; `java.time` kullanılmadı çünkü minSdk 24'te core library desugaring olmadan çalışmıyor ve bu adımda desugaring eklenmedi.

- Placeholder Alanlar: Yakıt yüzdesi, vites tipi ve koltuk sayısı için `VehicleDetailBottomSheet` ile aynı onaylanmış placeholder değerler (`PLACEHOLDER_FUEL_PERCENT=72`, `PLACEHOLDER_TRANSMISSION="Manuel"`, `PLACEHOLDER_SEAT_COUNT=5`) tekrar kullanıldı; yeni bir onay gerekmedi.

- Placeholder Fotoğraf: Araç fotoğrafı, `HomeScreen.kt`'deki `VehiclePhoto` ile aynı `car_{marka}_{model}` kaynak adı kuralına göre çözümleniyor.


### Araç Durumu Ekranı (Kiralama Öncesi Kontrol)

- Karar: Rezervasyon akışının devamı olarak `ui/vehiclecondition/` altında yeni bir ekran eklendi (`VehicleConditionContract.kt`, `VehicleConditionViewModel.kt`, `VehicleConditionScreen.kt`). Rezervasyon Onayı ekranında "Rezervasyonu Tamamla" başarılı olunca (`POST /rentals` başarılı), önceki davranış olan "Home'a dön + başarı snackbar'ı" yerine artık doğrudan bu Araç Durumu ekranına geçiliyor; `ReservationEffect.NavigateToVehicleCondition` gerçek `rentalId`'yi (`RentalResponseDto.id`) ve araç bilgilerini taşıyor.

- Son Güncelleme Tarihi: 06.07.2026

- Fotoğraf Çekimi (Mock, Kullanıcı Onayıyla): Gerçek kamera/galeri entegrasyonu bu adımda YAPILMADI — kullanıcının açık isteğiyle, 4 yön kartından (Ön/Arka/Sol/Sağ) birine dokunulduğunda gerçek bir fotoğraf çekilmeden o yön anında "çekildi" (yeşil onay) olarak işaretleniyor. Bu bilinçli bir mock'tur; gerçek kamera akışı (ör. CameraX + dosya/URI saklama) ayrı bir adımda ele alınacak.

- Akış: 4 yönün tamamı işaretlenmeden "Kiralamayı Başlat" butonu pasif kalıyor (buton etiketinde kalan foto sayısı gösteriliyor). Bu ekranda backend'e herhangi bir çağrı yapılmıyor — kiralama zaten `POST /rentals` ile Rezervasyon adımında oluşturulmuş durumda (`RentalResponseDto.status` sunucuda zaten ACTIVE); bu ekran yalnızca istemci tarafında bir "hazır mısın" kontrolü.

- Güncelleme (06.07.2026): "Kiralamayı Başlat" artık gerçekten "Kiralama Aktif" ekranına yönlendiriyor; bkz. aşağıdaki "Kiralama Aktif Ekranı" kararı.


### Kiralama Aktif Ekranı (Canlı Yolculuk)

- Karar: `ui/activerental/` altında yeni bir ekran eklendi (`ActiveRentalContract.kt`, `ActiveRentalViewModel.kt`, `ActiveRentalScreen.kt`). Araç Durumu ekranında "Kiralamayı Başlat" artık bu ekrana yönlendiriyor.

- Son Güncelleme Tarihi: 06.07.2026

- Veri Katmanı: `RentalRepository`'ye daha önce yalnızca `RentalService`'te tanımlı olup kullanılmayan `getRentalDetails(id)` ve `returnVehicle(id)` metotları eklendi (`VehicleRepository` ile aynı desen). Ekran açılışında `getRentalDetails` ile gerçek `startDate` çekiliyor; "Kiralamayı Bitir" gerçek `POST /rentals/{id}/return` çağrısını tetikliyor ve dönen gerçek `totalPrice` kullanılıyor.

- Geçen Süre: Gerçek `rental.startDate` (ISO 8601, UTC) `SimpleDateFormat` ile parse edilip şu anki zamanla farkı alınarak hesaplanıyor; her saniye `ActiveRentalIntent.Tick` ile yeniden hesaplanıyor. `java.time` kullanılmadı (minSdk 24, desugaring yok — Rezervasyon ekranındaki kararla aynı gerekçe).

- Anlık Ücret: Backend'de anlık/canlı bir ücret alanı olmadığından, gerçek `pricePerDay`'den orantılı türetiliyor (`pricePerDay / 1440 * geçenDakika`) — VehicleDetailBottomSheet ve Rezervasyon ekranındaki ile aynı, uydurma olmayan orantılı hesaplama yaklaşımı.

- Mesafe: Backend'de mesafe/rota verisi hiç yok. Bu ekran açıkken gerçek GPS güncellemeleri arasında Haversine ile hesaplanan gerçek mesafe biriktiriliyor (uygulama oturumuna özgü; ekrandan çıkılıp tekrar girilirse veya süreç öldürülürse sıfırlanır — kalıcı/sunucu tarafı bir mesafe kaydı yok). Kat edilen yolun haritada çizgi olarak gösterilmesi (mockup'taki noktalı iz) bu adımda kapsam dışı bırakıldı; yalnızca canlı konum (mavi nokta) gösteriliyor.

- Kilitle/Aç: `RentalService`'te karşılığı olan bir uç nokta yok; buton mevcut "Kilidi Aç" kararıyla tutarlı şekilde no-op bırakıldı.

- Güncelleme (06.07.2026): "Kiralamayı Bitir" artık gerçekten "Yolculuk Tamamlandı" ekranına yönlendiriyor; bkz. aşağıdaki karar.

- Bilinen Basitleştirme: Harita bileşeni (`ActiveRentalMapView`), Ana Harita'daki `RencarMapView`/`enableLocationComponent` ile neredeyse aynı kurulumu tekrar ediyor (ayrı dosyada, ortak bir bileşene çıkarılmadı); zaman baskısıyla bilinçli bir kod tekrarı, ileride ortak bir `ui/common/` haritakomponentine refactor edilebilir.


### Yolculuk Tamamlandı Ekranı (Ödeme Özeti) — Zincirin Son Ekranı

- Karar: `ui/tripsummary/` altında yeni bir ekran eklendi (`TripSummaryContract.kt`, `TripSummaryViewModel.kt`, `TripSummaryScreen.kt`). Kiralama Aktif ekranında "Kiralamayı Bitir" başarılı olunca (`POST /rentals/{id}/return` başarılı), artık doğrudan bu ekrana geçiliyor; `ActiveRentalEffect.NavigateToTripSummary` gerçek süre/mesafe/toplam ücreti taşıyor.

- Son Güncelleme Tarihi: 06.07.2026

- Kaldırılan Tasarım Öğeleri (Kullanıcı Onayıyla, Önceki İki Kararla Tutarlı): Tasarımdaki "Kiralama ücreti / Başlangıç ücreti / Hizmet bedeli / İndirim · İLKSÜRÜŞ" fiyat dökümü **eklenmedi** — bunların hiçbiri `RentalResponseDto`'da veya başka bir backend alanında karşılık bulmuyor. Yalnızca gerçek `totalPrice` (return çağrısının döndürdüğü) tek bir "Toplam" satırı olarak gösteriliyor.

- Cüzdan Entegrasyonu (Yeni Stub Genişletmesi): "Öde" butonu gerçek bir ödeme API'sine değil, projede zaten kabul edilmiş fake/stub `WalletRepository`'ye bağlandı. Cüzdan'ın tamamı zaten gerçek bir backend'e sahip değil (`FakeWalletRepository`, sabit `balance = 340.0`, bkz. "Backend Hazır Değilken Veri Katmanı" kararı); bu adımda `WalletRepository`/`FakeWalletRepository`'ye yeni bir `payFromBalance(amount, title)` metodu eklendi (mevcut `loadBalance`'ın ters yönlü karşılığı — bakiyeden düşer, gider işlemi olarak `transactions` listesine ekler, yetersiz bakiyede `Result.failure` döner). Bu, yeni bir backend sözleşmesi uydurmak değildir; zaten onaylı olan fake-katman deseninin genişletilmesidir.

- Kart Bilgisi: "Öde" ekranındaki kart etiketi (`VISA • 4291` vb.) `WalletRepository.getCards()`'tan gerçek (fake ama tutarlı) veriyle çekiliyor; ayrıca hardcoded bir kart metni yazılmadı. "Değiştir" butonu no-op (kart değiştirme akışı kapsam dışı).

- Ödeme Sonrası: Başarılı ödemede Home'a dönülüyor (`popUpTo(0){inclusive=true}` ile geri yığın temizleniyor — Rezervasyon → Araç Durumu → Kiralama Aktif → Yolculuk Tamamlandı zincirine geri dönülemez). Bu, üç ekranlık kiralama akışının (Rezervasyon Onayı → Araç Durumu → Kiralama Aktif → Yolculuk Tamamlandı) son adımıdır.

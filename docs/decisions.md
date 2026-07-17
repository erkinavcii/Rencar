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

- Güncelleme — `getLastLocation` Yerine `getCurrentLocation` (11.07.2026): `LocationEngine.getLastLocation(...)` yalnızca önbellekten okur; önbellek boşsa veya çok eski bir değer içeriyorsa (özellikle emülatörde gözlemlenen bir senaryo) ilk konum hiç gelmiyor ya da bayat kalıyordu. Google'ın resmi önerisi doğrultusunda (ve derste kullanılan referans implementasyonla aynı desende) `enableLocationComponent` artık ilk konumu `com.google.android.gms.location.FusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ...)` ile alıyor; bu API gerekirse önbelleği kullanır, gerekirse GPS/ağ sağlayıcısından zorla taze bir fix ister. Sürekli takip için `LocationEngine.requestLocationUpdates(...)` değişmeden korunuyor; yalnızca ilk/tek seferlik konum alma yöntemi değişti. MVI kurallarına uygun olarak bu çağrı, `Context` gerektirdiğinden ViewModel'e değil `HomeRoute` içindeki (Context'e sahip) `enableLocationComponent` fonksiyonuna eklendi; sonuç yine mevcut `HomeIntent.UserLocationChanged` intent'i ile ViewModel'e iletiliyor, yeni bir Intent/Effect türü eklenmedi.

- Yeni Bağımlılık: `com.google.android.gms:play-services-location:21.3.0` (`gradle/libs.versions.toml` + `app/build.gradle.kts`). Sebep: MapLibre'nin kendi `LocationEngine` soyutlaması yalnızca `getLastLocation`/`requestLocationUpdates` sunuyor, `getCurrentLocation` yok; bu API yalnızca doğrudan `FusedLocationProviderClient`'ta mevcut ve projede daha önce hiç (transitive olarak da) yoktu.


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

- Bilinen Basitleştirme (Çözüldü — 14.07.2026): Harita bileşeni (`ActiveRentalMapView`), Ana Harita'daki `RencarMapView`/`enableLocationComponent` ile neredeyse aynı kurulumu tekrar ediyordu (ayrı dosyada, ortak bir bileşene çıkarılmamıştı); zaman baskısıyla bilinçli bir kod tekrarıydı. Bkz. "Harita Bileşeni — Ortak `ui/common/map` Paketine Çıkarılması" kararı.


### Yolculuk Tamamlandı Ekranı (Ödeme Özeti) — Zincirin Son Ekranı

- Karar: `ui/tripsummary/` altında yeni bir ekran eklendi (`TripSummaryContract.kt`, `TripSummaryViewModel.kt`, `TripSummaryScreen.kt`). Kiralama Aktif ekranında "Kiralamayı Bitir" başarılı olunca (`POST /rentals/{id}/return` başarılı), artık doğrudan bu ekrana geçiliyor; `ActiveRentalEffect.NavigateToTripSummary` gerçek süre/mesafe/toplam ücreti taşıyor.

- Son Güncelleme Tarihi: 06.07.2026

- Kaldırılan Tasarım Öğeleri (Kullanıcı Onayıyla, Önceki İki Kararla Tutarlı): Tasarımdaki "Kiralama ücreti / Başlangıç ücreti / Hizmet bedeli / İndirim · İLKSÜRÜŞ" fiyat dökümü **eklenmedi** — bunların hiçbiri `RentalResponseDto`'da veya başka bir backend alanında karşılık bulmuyor. Yalnızca gerçek `totalPrice` (return çağrısının döndürdüğü) tek bir "Toplam" satırı olarak gösteriliyor.

- Cüzdan Entegrasyonu (Yeni Stub Genişletmesi): "Öde" butonu gerçek bir ödeme API'sine değil, projede zaten kabul edilmiş fake/stub `WalletRepository`'ye bağlandı. Cüzdan'ın tamamı zaten gerçek bir backend'e sahip değil (`FakeWalletRepository`, sabit `balance = 340.0`, bkz. "Backend Hazır Değilken Veri Katmanı" kararı); bu adımda `WalletRepository`/`FakeWalletRepository`'ye yeni bir `payFromBalance(amount, title)` metodu eklendi (mevcut `loadBalance`'ın ters yönlü karşılığı — bakiyeden düşer, gider işlemi olarak `transactions` listesine ekler, yetersiz bakiyede `Result.failure` döner). Bu, yeni bir backend sözleşmesi uydurmak değildir; zaten onaylı olan fake-katman deseninin genişletilmesidir.

- Kart Bilgisi: "Öde" ekranındaki kart etiketi (`VISA • 4291` vb.) `WalletRepository.getCards()`'tan gerçek (fake ama tutarlı) veriyle çekiliyor; ayrıca hardcoded bir kart metni yazılmadı. "Değiştir" butonu no-op (kart değiştirme akışı kapsam dışı).

- Ödeme Sonrası: Başarılı ödemede Home'a dönülüyor (`popUpTo(0){inclusive=true}` ile geri yığın temizleniyor — Rezervasyon → Araç Durumu → Kiralama Aktif → Yolculuk Tamamlandı zincirine geri dönülemez). Bu, üç ekranlık kiralama akışının (Rezervasyon Onayı → Araç Durumu → Kiralama Aktif → Yolculuk Tamamlandı) son adımıdır.


### Harita Bileşeni — Ortak `ui/common/map` Paketine Çıkarılması

- Karar: Ana Harita (`RencarMapView`) ve Kiralama Aktif (`ActiveRentalMapView`) ekranlarındaki neredeyse birebir aynı MapLibre kurulum/lifecycle/marker/kamera/konum kodu, tek bir `ui/common/map/` paketine çıkarıldı. Bu paket herhangi bir ekranın ViewModel'ini, State/Intent/Effect sözleşmesini veya domain modelini (`VehicleMarker` gibi) bilmez; yalnızca saf, parametrik composable ve fonksiyonlardan oluşur.

- Son Güncelleme Tarihi: 14.07.2026

- Kapsam: `RencarMap.kt` (MapLibre kurulum/lifecycle/style composable'i + `GeoPoint`), `RencarMapMarkers.kt` (`MapMarkerItem` + `renderMapMarkers`, generic etiket/renk tabanlı marker çizimi), `RencarMapCamera.kt` (`fitCameraToPoints`), `RencarMapLocation.kt` (`enableLiveLocation`, Home ve ActiveRental'ın konum dinleme mantığının birleşimi, `cameraMode` parametrik).

- Feature'a Özel Kalanlar: Araç kategorisine/kullanımda durumuna göre marker rengi seçimi (`VehicleMarker.toMapMarkerItem`) bilinçli olarak `HomeScreen.kt` içinde bırakıldı, ortak pakete taşınmadı — bu Home'a özel bir iş kuralı, ortak paketin genel/domain-agnostic kalması gerekiyordu.

- Veri Kaynağından Bağımsızlık: Ortak paketteki composable ve fonksiyonlar, marker/konum verisinin nereden geldiğini (tek seferlik `GET /vehicles` listesi mi, ileride bir WebSocket akışı mı) bilmez; yalnızca kendilerine verilen `List<MapMarkerItem>`/konum callback'ini render eder. Canlı araç konumu için bir WebSocket entegrasyonu ileride eklenirse, bu paket değişmeden kalması beklenir — yalnızca ilgili ekranın ViewModel/Repository katmanı güncellenir.

- Düzeltilen Yan Etki: `enableLiveLocation` artık bir dispose fonksiyonu döndürüyor. ActiveRental'ın önceki `enableLiveLocation` çağrısı `LaunchedEffect` içindeydi ve konum güncellemelerini hiçbir zaman durdurmuyordu (kaynak sızıntısı); artık her iki ekranda da `DisposableEffect` ile doğru şekilde dispose ediliyor.

- Bilinçli Sınır: Bu refactor yalnızca harita render/konum katmanını birleştirir; canlı araç konumu için bir WebSocket veri kaynağı bu kararın kapsamında değildir, ayrı bir karar olarak ele alınmalıdır.


### Kiralama Aktif Ekranı — Eksik Konum İzni Kontrolü Düzeltildi

- Karar: `ActiveRentalScreen.kt`, harita refactor'üne kadar konum iznini hiç kontrol etmiyordu (Ana Harita'nın aksine); `activateLocationComponent` doğrudan izin varsayımıyla çağrılıyordu (`@SuppressLint("MissingPermission")` olmadan). Bu, yukarıdaki harita paketi refactor'ü sırasında fark edilen ayrı bir hataydı; refactor'den bağımsız, kendi başına bir düzeltmedir.

- Son Güncelleme Tarihi: 14.07.2026

- Uygulama: `ActiveRentalRoute`'a, Ana Harita'daki ile aynı desende bir izin akışı eklendi (`rememberLauncherForActivityResult` + `ContextCompat.checkSelfPermission`). Sonuç, `ActiveRentalViewModel`/`ActiveRentalContract`'a değil, yalnızca yerel Compose state'ine (`hasLocationPermission`) yazılıyor ve `ActiveRentalScreen` → `ActiveRentalMapView` parametre zinciriyle taşınıyor. `enableLiveLocation` artık yalnızca izin varken çağrılıyor.

- Kapsam Dışı Bırakılan: İznin `ActiveRentalUiState`'e taşınması (Home'daki `hasLocationPermission` alanıyla aynı desen) bu adımda yapılmadı — bu, Contract/ViewModel değişikliği gerektirir ve harita refactor'ünün onaylanan dosya kapsamının (yalnızca `ActiveRentalScreen.kt`) dışındadır. İleride gerekirse ayrı bir adımda ele alınabilir.


### Rental Veri Katmanı — API v2 Plan Bazlı Yaşam Döngüsü (Batch 2)

- Karar: `data/model/RentalDtos.kt`, `data/remote/RentalService.kt`, `data/repository/RentalRepository.kt`+`Impl.kt` API v2'nin plan bazlı kiralama sözleşmesine taşındı. `CreateRentalDto`'ya `plan` (`PER_MINUTE`/`HOURLY`/`DAILY`, varsayılan `DAILY` — geriye uyumlu) eklendi; `RentalResponseDto` genişledi (`vehicle`, `plan`, `startedAt`, `endedAt`, `startFee`, `serviceFee`, `discountAmount`, `distanceKm`, `durationMinutes`, `paymentStatus`, `paymentMethod`); `status` enumuna `PREPARING` eklendi. Yeni DTO'lar: `FinishRentalResponseDto`, `PayRentalDto`/`PayRentalResponseDto`, `ActiveRentalResponseDto`, `RentalStatsResponseDto`, `RentalPhotoDto`/`RentalPhotosStateDto`. Yeni uçlar: `start`, `finish`, `pay`, `photos` (GET+POST multipart), `active`, `stats`, `DELETE rentals/{id}` (yalnız `PREPARING`). Eski `POST rentals/{id}/return` korundu — yalnız `DAILY` planı için geçerli.

- Son Güncelleme Tarihi: 16.07.2026

- Nullable Alan Kararı (canlı API'de doğrulanmış): `endDate`, `endedAt`, `totalPrice`, `serviceFee`, `paymentMethod` nullable modellendi çünkü `PREPARING`/`PER_MINUTE`/`HOURLY` durumlarında bu alanlar sunucudan `null` gelebiliyor (ör. `totalPrice`, `PER_MINUTE`/`HOURLY`'de yalnızca `finish` sonrası kilitleniyor). `startDate`/`startedAt` ise backend'in ikisini de her zaman aynı anda döndürdüğü doğrulandığından (eski istemci uyumluluğu için `startDate` korunuyor) non-null bırakıldı. `@SerialName` kullanılmadığından Kotlin alan adları JSON alan adlarıyla birebir eşleşiyor.

- Geriye Uyumluluk (mevcut tüketiciler): `RentalResponseDto`'nun nullable alanları nedeniyle üç dosyada null-safety düzeltmesi yapıldı — `data/history/HistoryRepositoryImpl.kt` (`totalPrice ?: 0.0`; süre hesabı artık backend'in `durationMinutes` alanından, plan-bağımsız `endDate` üzerinden değil), `ui/activerental/ActiveRentalViewModel.kt` (`derivePricePerDay` yalnızca DAILY planında `totalPrice`/`endDate` doluyken hesap yapar, diğerlerinde `null` döner), `ui/vehiclecondition/VehicleConditionViewModel.kt` (`totalPrice ?: 0.0`). Bu üç dosya dışında hiçbir tüketicinin (`ReservationViewModel`, `HomeViewModel`, `di/RentalModule.kt`) davranışı değişmedi; `createRental` imzasına eklenen `plan` parametresi sona eklenip varsayılan değer verildiği için mevcut pozisyonel çağrılar bozulmadı.

- Kapsam Dışı Bırakılan: Bu batch yalnızca veri katmanını (DTO/Service/Repository) kapsar. Yeni uçların (`start`/`finish`/`pay`/`photos`/`active`/`stats`) UI'dan gerçekten çağrılması ayrı adımlarda ele alınacak: rezervasyon akışında plan seçimi ve gerçek fotoğraf yükleme, aktif kiralama ekranında sunucu taraflı anlık ücret/mesafe, ödeme entegrasyonu (iyzico entegrasyonu hoca tarafından anlatıldıktan sonra eklenecek — bu nedenle `payRental` şu an hiçbir ekrandan çağrılmıyor, yalnızca veri katmanında hazır bekliyor).

- Bilinen Backend Sınırı: `GET /rentals` ve `GET /rentals/stats` canlı API'de `500` dönüyor (backend hatası, istemci kaynaklı değil); `HistoryRepositoryImpl.getHistory()` hâlâ `GET /rentals`'a bağımlı olduğundan Geçmiş ekranı bu hata düzelene kadar risklidir.


### Rezervasyon Onayı Ekranı — Kiralama Açmadan Önce Zorunlu Rezervasyon (Geçici Bağlantı)

- Karar: Canlı API'de doğrulandı — `POST /rentals`, aracın üzerinde AKTİF bir rezervasyon yoksa `409` ile reddediyor ("kilidi açmak için önce aracı rezerve etmelisiniz"); başarılı olursa rezervasyon `CONVERTED` işaretlenip araç `RESERVED` → `RENTED` geçiyor. `ReservationViewModel.confirmReservation()` bu kurala kadar doğrudan `rentalRepository.createRental(...)` çağırıyordu ve rezervasyon adımı hiç yapılmıyordu; bu yüzden "Rezervasyonu Tamamla" butonu her zaman 409 ile başarısız oluyordu.

- Son Güncelleme Tarihi: 16.07.2026

- Uygulama: `ReservationViewModel`'e `ReservationRepository` (Batch 3'te yazılmış ama hiçbir ekrandan çağrılmayan veri katmanı) enjekte edildi. `confirmReservation()` artık önce `reservationRepository.reserveVehicle(vehicleId)` çağırıyor; yalnızca bu başarılı olursa `rentalRepository.createRental(...)`'a geçiyor. Rezervasyon başarısız olursa `createRental` hiç çağrılmadan `ReservationEffect.ShowError` gönderiliyor.

- Bilinçli Sınırlama (Geçici): Bu, Batch 5'in tam kapsamı (15 dakikalık ücretsiz tutma süresini gösteren ayrı bir "ReservationHold" geri sayım ekranı, plan seçimine göre `PER_MINUTE`/`HOURLY`/`DAILY` eşlemesi) değildir — yalnızca akışı çalışır hale getiren minimum bağlantıdır. Kullanıcı rezervasyon süresi (varsayılan 15 dk) dolmadan "Rezervasyonu Tamamla"ya basmazsa mevcut davranışta bir geri sayım/uyarı gösterilmiyor; zaten aktif bir rezervasyonu varsa (ör. ekrana tekrar girildiyse) `reserveVehicle` 409 ile başarısız olur ve kullanıcı hata mesajı görür. Bu senaryoların düzgün ele alınması (rezervasyonu iptal etme, geri sayım UI'ı) Batch 5'e bırakıldı.


### Ana Harita — Aktif Kiralamaya Dönüş Banner'ı `GET /rentals/active`'e Taşındı

- Karar: `HomeViewModel.checkActiveRental()`, aktif kiralamayı tespit etmek için `rentalRepository.getMyRentals()` (`GET /rentals`) çağırıyordu. Bu uç nokta canlı API'de `500` döndüğü için (yukarıdaki "Bilinen Backend Sınırı" kararı) banner hiçbir zaman görünmüyordu; kullanıcı kiralama akışının ortasında (ör. Araç Durumu/foto ekranından geri tuşuyla çıkınca) Home'a düşüyor, ne yeni bir araç rezerve edebiliyor (zaten aktif kiralaması olduğu için 409) ne de aktif kiralamasına geri dönebiliyordu (banner görünmediği için). Bu canlı testte ortaya çıktı ve tespit edildi.

- Son Güncelleme Tarihi: 16.07.2026

- Uygulama: `RentalRepository.getActiveRental()`'in dönüş tipi `Result<ActiveRentalResponseDto>` → `Result<ActiveRentalResponseDto?>` yapıldı; `RentalRepositoryImpl` 404'ü (aktif kiralama yok — Batch V'de doğrulanmış normal durum) `Result.success(null)` olarak, diğer tüm hataları `Result.failure` olarak modelliyor. `HomeViewModel.checkActiveRental()` artık `getMyRentals()` yerine bunu çağırıyor. Ayrıca `GET /rentals/active` cevabında araç özeti (`vehicle: RentalVehicleSummaryDto`) zaten gömülü geldiğinden, önceden yapılan ayrı `vehicleRepository.getVehicleDetails(vehicleId)` çağrısı (`GET /vehicles/{id}`, yalnızca AVAILABLE araçları döndürdüğü için RENTED — yani tam da aktif kiralaması olan — bir araç için hep 404 dönüyordu) kaldırıldı.

- Bilinen Sınır (değişmedi): `ActiveRentalVehicle.pricePerDay` alanı `RentalVehicleSummaryDto`'da bulunmadığından `0.0` ile dolduruluyor; banner'da fiyat gösterilmiyor, yalnızca marka/model/plaka gösteriliyor (önceki davranışla aynı — vehicle detayı hiç alınamadığında zaten aynı sonuç oluyordu).

- Kapsam Dışı Bırakılan: `HistoryRepositoryImpl.getHistory()` hâlâ `GET /rentals`'a bağımlı (Geçmiş ekranının kendisi liste görünümü gerektiriyor, `/rentals/active` bunun yerine geçemez); bu, backend'in `500` hatasını düzeltmesini bekliyor.


### Ehliyet Doğrulama — Cihaz Üzerinde Yüz Eşleştirme + "AI ile Anında Onayla"

- Karar: Ehliyet ön yüz fotoğrafı ile selfie'yi karşılaştırmak için Yöntem A (cihaz üzerinde ML Kit + TensorFlow Lite) seçildi; sunucu tarafı köprü (Yöntem B, ayrı Python mikroservis + ngrok) MVP kapsamı dışında bırakıldı. Gerekçe, risk matrisi ve tam mimari karşılaştırma için bkz. `docs/ml-face-matching.md`.

- Son Güncelleme Tarihi: 17.07.2026

- Uygulama: Yüz eşleştirme, upload'ı engelleyen bir kapı değil; `UNDER_REVIEW` durumundaki başvuruda görünen opsiyonel bir "AI ile Anında Onayla" butonudur. Eşik (`FaceMatcher.THRESHOLD`) geçilirse backend'de tanımlı sabit bir demo admin hesabıyla (`+905550000000`, simüle OTP `123456`) anlık login yapılıp `PATCH /admin/licenses/{id}/approve` çağrılır; geçilmezse veya buton hiç kullanılmazsa başvuru normal admin incelemesinde kalır — bu akış hiçbir şekilde engellenmez.

- Threshold Notu: Kimlik üzerindeki yüzün küçüklüğü, fine-tune edilmemiş genel model ve selfie/kimlik arasındaki açı farkı nedeniyle eşik akademik değerlerin (ör. 0.70) çok altında, `0.35`'e çekildi — bilinçli olarak false-negative'i azaltmayı önceliklendiren bir demo/MVP tercihi. `FaceMatcher` her çağrıda gerçek skoru Logcat'e yazar; gerçek cihaz testinden sonra kalibre edilmesi gerekir. Ayrıntı: `docs/ml-face-matching.md` §2.

- Bilinçli Güvenlik Sınırlaması (yalnız demo/MVP): Admin onayı istemciden tetikleniyor. Bunun neden gerçek bir üründe kabul edilemez olduğu (decompile edilebilir yetki, istemci tarafı bypass riski) ve prod'da doğru mimarinin (Yöntem B — onay kararının sunucu tarafında verilmesi) ne olduğu `docs/ml-face-matching.md`'de ayrıntılı belgelendi.

- Mimari Not: Admin onay çağrısı, ana ağ katmanından (`NetworkModule`) kasıtlı olarak izole tutuldu (`di/AdminApprovalModule.kt`, ayrı `OkHttpClient`/`Retrofit`). Sebep: `AuthInterceptor` her isteğe müşterinin `TokenManager`'daki access token'ını koşulsuz bastığından, admin çağrısı paylaşılan istemciden gitseydi müşteri token'ı admin token'ın yerini alır ve çağrı `403` dönerdi. İzolasyon ayrıca müşteri oturumunu (refresh token rotasyonu, `SessionManager`) bu yan akıştan tamamen korur.

### Kiralama Aktif Ekranı — Aracın Canlı Konumu (Socket.IO)

- Karar: Kiralama Aktif ekranında, kiracının kendi telefon GPS'inin yanına, aracın backend'den gerçek zamanlı yayınlanan konumu da eklendi. Bağlantı protokolü **Socket.IO** (`io.socket:socket.io-client:2.1.2`, `org.json` modülü exclude edilerek — Android platformunda zaten mevcut).

- Son Güncelleme Tarihi: 17.07.2026

- Sözleşme: `/ws/locations` namespace'ine kullanıcının access token'ıyla (`auth.token`) bağlanılır; sunucu yalnızca kullanıcının aktif kiralamasındaki aracın karesini `my-vehicle` event'iyle gönderir (payload: `{ ts, vehicle: { vehicleId, latitude, longitude, ... } }`). `rentalId` istemciden parametre olarak geçilmez, sunucu token üzerinden çözer. Bu sözleşme, referans bir Kotlin örneğinden (`RideLocationClient.kt` gist'i) çıkarılmış, gerçek bir uydurma değildir.

- Veri Katmanı: `data/remote/RideLocationClient.kt` (yeni) — `vehiclePositionStream(): Flow<VehicleLocationPoint>`. `RentalRepository`/`RentalRepositoryImpl`'e aynı imzalı bir metot eklenip bu istemciye delege edildi. `VehicleLocationPoint` (`data/model/RentalDtos.kt`) `@Serializable` değildir; payload Retrofit üzerinden değil, doğrudan `org.json.JSONObject` ile elle parse edilir (gist'teki `parsePoint` deseniyle birebir).

- Referans Örnekten Sapmalar (uydurma değil, projedeki gerçek karşılıklarla değiştirildi): `BuildConfig.BASE_URL` yerine projede zaten var olan `ApiConfig.BASE_URL` kullanıldı (sondaki `/` çift slash'a yol açmasın diye `trimEnd('/')` uygulandı). `TokenStore` yerine projedeki `TokenManager` kullanıldı. Örnekteki `SessionManager.refreshSession()` projede yok; bunun yerine `TokenAuthenticator.kt`'deki (401 yenileme) ile birebir aynı mantık (`AuthService.refreshTokens` + `TokenManager.saveTokens`) doğrudan `RideLocationClient` içine yazıldı. Ayrıca, örnekte olmayan bir iyileştirme olarak, refresh de başarısız olursa `TokenManager.clearTokens()` + `SessionManager.notifySessionExpired()` çağrılır — bu, REST katmanındaki 401 sonrası oturum sonlandırma davranışıyla (bkz. "Oturum Süresi Dolması (401)" kararı) tutarlılık için eklendi.

- UI/Mimari: `domain/model` paketi projede hiç olmadığından `VehicleLocationPoint` `data/model/` altında tutuldu. `ActiveRentalContract`/`ActiveRentalViewModel`, `ui/common/map` paketine (Compose/Context bağımlı) bağımlı olmasın diye konum, var olan `ActiveRentalLatLng` tipiyle taşınıyor; `GeoPoint`/`MapMarkerItem` dönüşümü yalnızca `ActiveRentalScreen.kt` içinde, `HomeScreen.kt`'deki aynı desenle (`toMapMarkerItem`) tutarlı şekilde yapılıyor. `ActiveRentalViewModel.init` içinde `rentalRepository.vehiclePositionStream()` doğrudan toplanıyor (Context gerekmediğinden, `loadRentalStart()` ile aynı yaklaşım); ayrı bir Intent eklenmedi.

- Kapsam: Yalnızca Kiralama Aktif ekranı. Ana Harita'daki müsait araçların websocket ile canlı güncellenmesi bu kararın kapsamında değildir (bkz. "Harita Bileşeni — Ortak `ui/common/map` Paketine Çıkarılması" kararındaki not); ayrı bir karar olarak ele alınmalıdır. Bağlantı kopunca kullanıcıya görünür bir hata/snackbar da eklenmedi — akış sessizce kapanır.

- Dal: Bu iş `feature/ana-harita-map` (zaten `main`'e PR #27 ile merge edilmişti) üzerinde değil, güncel `main`'den açılan yeni `feature/canli-arac-konumu` dalında yapıldı; `main` bu dala göre "Rental API v2" geçişini (`ApiConfig`, `ActiveRentalResponseDto`, vb.) zaten içeriyordu.

- Build Sınırı (AGP 9 + TFLite namespace çakışması): ML Kit/TFLite bağımlılıkları eklenince `processDebugMainManifest` şu hatayla patlıyordu: `Namespace 'org.tensorflow.lite' is used in multiple modules and/or libraries: org.tensorflow:tensorflow-lite:2.14.0, org.tensorflow:tensorflow-lite-api:2.14.0` (aynı şekilde `org.tensorflow.lite.support` için support/support-api). Kök neden bizim kodumuzda değil, TFLite'ın kendi yayınladığı AAR'larda — `tensorflow-lite` ve `tensorflow-lite-api` aynı namespace'i paylaşıyor, AGP 9 namespace benzersizliğini artık sıkı denetlediğinden bu hataya dönüşüyor. İki bağımsız GitHub issue'su (`tensorflow/tensorflow#109508`, `google-ai-edge/LiteRT#6965`) aynı senaryoyu doğruluyor ve tek geçici çözümü işaret ediyor: `gradle.properties`'e `android.uniquePackageNames=false` eklendi. Bu bayrak AGP'nin kendi notlarında "kısa vadeli, gelecekte kaldırılabilir" olarak işaretli; kalıcı düzeltme kütüphane bakımcılarının namespace'leri benzersizleştirmesiyle gelecek. Android Studio'da rebuild ile doğrulandı.

- Bilinen Sınır (16 KB sayfa boyutu uyumluluğu, aksiyon gerekmez): Cihazda çalıştırınca Android, dört native kütüphanenin (`libface_detector_v2_jni.so` — ML Kit, `libtensorflowlite_jni.so` — TFLite, `libmaplibre.so`, `libandroidx.graphics.path.so`) 16 KB bellek sayfası sınırına hizalı olmadığını bildiren bir uyarı gösteriyor; uygulama "page size compatible mode"da sorunsuz çalışıyor, bu bir hata değil. Dördü de üçüncü parti AAR'ların kendi derledikleri binary'ler — bizim kodumuzda düzeltilecek bir şey yok, kütüphane sahiplerinin (Google/MapLibre/TensorFlow) yeni sürüm yayınlaması gerekiyor. Google Play, hedef API'ye göre hizalanmamış yeni yükleme/güncellemeleri Kasım 2025'ten itibaren reddetmeye başladı; bu proje Play'e yayınlanmayacağı (jüri demosu) için şimdilik aksiyon gerekmiyor, yalnızca gelecekte Play dağıtımı gündeme gelirse hatırlanmalı.

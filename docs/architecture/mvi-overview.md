# Rencar - MVI Mimarisi (Genel Bakış)

> Bu dosya Rencar'da MVI (Model-View-Intent) mimarisinin **tek doğruluk kaynağıdır**
> (single source of truth). Yeni bir ekran/feature MVI ile yazılırken buradaki prensipler,
> veri akışı ve katman sorumlulukları **zorunludur**. Sözleşme (Contract) ve ViewModel'e özel
> kurallar için bkz. [mvi-contracts.md](mvi-contracts.md) ve [mvi-viewmodel-rules.md](mvi-viewmodel-rules.md).
>
> Referans implementasyon: Login ekranı (`ui/auth/login/`).

---

## 1. Temel Kural

> Her ekran üç parçadan oluşur: **State** (durum), **Intent** (kullanıcı niyeti),
> **Effect** (tek seferlik olay). Veri akışı **tek yönlüdür**: UI yalnızca Intent yayar,
> ViewModel State üretir ve Effect gönderir. UI hiçbir zaman iş mantığı çalıştırmaz veya
> durum sahibi olmaz.

---

## 2. Veri Akışı (Tek Yönlü)

```
Kullanıcı etkileşimi
        ↓  onIntent(Intent)
        ▼
   ViewModel  ──(viewModelScope)───►  Repository (suspend)
        │                                   │
        │  _uiState.update { ... }          │  Result<…>
        ▼                                   ▼
   StateFlow<UiState>            Channel<Effect> (one-shot)
        │  collectAsStateWithLifecycle()    │  receiveAsFlow()
        ▼                                   ▼
   <Screen>Screen (durumsuz)        <Screen>Route → LaunchedEffect
```

- **State**: Sürekli gözlemlenen, immutable, tekil durum. `StateFlow` ile yayılır.
- **Effect**: Bir kez tüketilen olay (navigasyon, snackbar). `Channel` + `receiveAsFlow` ile
  yayılır; **asla State içinde tutulmaz** (konfigürasyon değişiminde tekrar tetiklenmemesi için).
- **Intent**: UI'dan ViewModel'e giden tek niyet kanalı. Tek giriş noktası `onIntent(...)`.

---

## 3. Katman Sorumlulukları

| Katman | Sorumluluk | Yapamayacağı |
|--------|------------|--------------|
| **UI (Screen)** | Durumsuz çizim, Intent yayma | İş mantığı, repository çağrısı, state sahipliği |
| **UI (Route)** | ViewModel'i alma, state toplama, Effect tüketme | İş mantığı |
| **ViewModel** | Intent işleme, state üretme, Effect gönderme, repository orkestrasyonu | Android/Compose/Context bağımlılığı |
| **Repository** | Veri kaynağı soyutlaması (ağ/yerel) | UI/ViewModel'e bağımlılık |
| **DI (Module)** | Bağımlılık bağlama (`@Binds`/`@Provides`) | İş mantığı |

---

## 4. Paket ve Dosya Yapısı

Bir `<Feature>` / `<Screen>` için zorunlu yerleşim:

```
com.turkcell.rencar_pair/
├── ui/<feature>/<screen>/
│   ├── <Screen>Contract.kt   // UiState + Intent + Effect (tek dosya)
│   ├── <Screen>ViewModel.kt  // @HiltViewModel
│   └── <Screen>Screen.kt     // <Screen>Route (stateful) + <Screen>Screen (stateless)
├── data/<feature>/
│   ├── <X>Repository.kt      // interface
│   └── <Impl>Repository.kt   // implementasyon (@Inject constructor)
└── di/
    └── <X>Module.kt          // @Module @InstallIn(SingletonComponent) @Binds
```

Referans karşılıkları: `LoginContract.kt`, `LoginViewModel.kt`, `LoginScreen.kt`,
`data/auth/AuthRepository.kt`, `data/auth/FakeAuthRepository.kt`, `di/AuthModule.kt`.

---

## 5. Dependency Injection (Hilt)

- Uygulama sınıfı `@HiltAndroidApp` ile işaretlenir (`RencarApplication`) ve `AndroidManifest`
  içinde `android:name` ile bağlanır.
- ViewModel barındıran Activity `@AndroidEntryPoint` ile işaretlenir.
- ViewModel Compose'a `hiltViewModel()` ile alınır
  (`androidx.hilt.navigation.compose` paketinden).
- Navigasyon tek bir `NavHost` üzerinden yürütülür: `ui/navigation/RencarNavHost.kt`.
- DI ile ilgili karar geçmişi ve sürümler için bkz. [../decisions.md](../decisions.md).

---

## 6. Backend Henüz Hazır Değilken (Stub Repository)

Backend API sözleşmesi tanımlı değilse (`agents.md` §2.2 — uydurmak yasak):

- Repository **interface** olarak tanımlanır.
- Geçici olarak sahte bir implementasyon (`Fake<X>Repository`) yazılır; ağ davranışı
  `delay(...)` ile taklit edilir.
- Gerçek API geldiğinde **yalnızca implementasyon** ve `di/<X>Module.kt` bağlama hedefi değişir;
  ViewModel ve Contract değişmez.

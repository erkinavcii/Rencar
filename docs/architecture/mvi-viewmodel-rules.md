# Rencar - ViewModel ve UI Bağlama Kuralları

> ViewModel, UI bağlama (Route/Screen) ve DI için **tek doğruluk kaynağıdır**.
> Sözleşme için bkz. [mvi-contracts.md](mvi-contracts.md), genel akış için [mvi-overview.md](mvi-overview.md).
>
> Referans: `ui/auth/login/LoginViewModel.kt`, `LoginScreen.kt`, `di/AuthModule.kt`.

---

## 1. Temel Kural

> ViewModel'in UI ile tek temas noktası `fun onIntent(intent: <Screen>Intent)`'tir.
> State `StateFlow`, Effect `Channel` ile dışarı açılır; her ikisinin de mutable hali
> **private** tutulur. ViewModel içinde **hiçbir Android/Compose/Context bağımlılığı bulunamaz.**

---

## 2. ViewModel İskeleti

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged -> updateForm { it.copy(phoneNumber = intent.value) }
            is LoginIntent.PasswordChanged    -> updateForm { it.copy(password = intent.value) }
            is LoginIntent.TogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is LoginIntent.Submit -> submit()
        }
    }
}
```

---

## 3. Zorunlu Kurallar

1. `@HiltViewModel` + `@Inject constructor`. Bağımlılıklar `private val` olarak alınır.
2. `MutableStateFlow` private; dışarı `asStateFlow()` ile `StateFlow` olarak açılır.
3. Tek seferlik olaylar `Channel(Channel.BUFFERED)` private; dışarı `receiveAsFlow()` ile açılır.
4. Tek giriş noktası `onIntent(...)`; `when` **tüm** Intent dallarını kapsar (exhaustive).
   Dallar `is ...` biçiminde yazılır.
5. Asenkron iş `viewModelScope.launch { ... }` içinde, repository üzerinden yapılır.
6. **Yasak:** ViewModel içinde `Context`, `View`, `@Composable`, `Activity`, navigasyon API'si.
7. State güncellemeleri yalnızca `_uiState.update { it.copy(...) }` ile yapılır (atomik).

---

## 4. Türetilen Alanlar

Buton aktifliği gibi türetilen alanlar, ilgili alan her değiştiğinde yeniden hesaplanır.
Türetme tek bir yardımcıda toplanır:

```kotlin
private fun updateForm(transform: (LoginUiState) -> LoginUiState) {
    _uiState.update { current ->
        val updated = transform(current)
        updated.copy(isLoginEnabled = updated.isFormValid())
    }
}

private fun LoginUiState.isFormValid(): Boolean =
    phoneNumber.isNotBlank() && password.isNotBlank()
```

---

## 5. Asenkron Akış ve Effect

- İşlem başlarken/biterken `isLoading` güncellenir; çift gönderim için erken `return` ile korunur.
- Sonuç `Result<…>` üzerinden ele alınır; başarı/hata Effect'e dönüştürülür.

```kotlin
private fun submit() {
    val state = _uiState.value
    if (!state.isLoginEnabled || state.isLoading) return
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        val result = authRepository.login(state.phoneNumber, state.password)
        _uiState.update { it.copy(isLoading = false) }
        result
            .onSuccess { _effect.send(LoginEffect.NavigateToHome) }
            .onFailure { _effect.send(LoginEffect.ShowError(it.message ?: "Giriş başarısız.")) }
    }
}
```

---

## 6. UI Bağlama: Route + Screen

İki composable zorunludur:

- **`<Screen>Route`** (durumlu/stateful): ViewModel'i `hiltViewModel()` ile alır, state'i
  `collectAsStateWithLifecycle()` ile toplar, Effect'leri `LaunchedEffect` içinde tüketir.
  Tek MVI köprüsü burasıdır.
- **`<Screen>Screen`** (durumsuz/stateless): `state: <Screen>UiState` ve
  `onIntent: (<Screen>Intent) -> Unit` parametrelerini alır; preview edilebilir olmalıdır.

```kotlin
@Composable
fun LoginRoute(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                LoginEffect.NavigateToHome -> { /* RencarNavHost üzerinden bağlanır */ }
            }
        }
    }
    LoginScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}
```

**Yasak:** `<Screen>Screen` içinde ViewModel referansı, repository çağrısı veya iş mantığı.
UI yalnızca `onIntent(...)` ile niyet yayar.

---

## 7. DI Modülü (Repository Bağlama)

Interface → implementasyon bağlaması `di/<X>Module.kt` içinde `@Binds` ile yapılır:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository
}
```

- Implementasyon sınıfı `@Inject constructor` ile inject edilebilir olmalıdır.
- Gerçek API geldiğinde yalnızca `@Binds` hedefi değiştirilir.

---

## 8. Navigasyon Entegrasyonu

- Tüm Route composable'lar `ui/navigation/RencarNavHost.kt` içindeki tek `NavHost`'a kayıt edilir.
- Navigasyon Effect akışı üzerinden tetiklenir: ViewModel `NavigateTo*` Effect'i gönderir,
  Route bunu tüketir ve `RencarNavHost`'tan gelen lambda'yı çağırır.
- ViewModel içinde `NavController` veya `NavHostController` referansı **kesinlikle bulunmaz**.

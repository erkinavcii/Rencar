# Rencar - MVI Sözleşmesi (Contract) Kuralları

> Bir ekranın State + Intent + Effect tanımları için **tek doğruluk kaynağıdır**.
> Genel akış için bkz. [mvi-overview.md](mvi-overview.md).
>
> Referans: `ui/auth/login/LoginContract.kt`.

---

## 1. Temel Kural

> Her ekranın State, Intent ve Effect tanımları **tek bir dosyada** toplanır:
> `<Screen>Contract.kt`. State bir `data class`, Intent ve Effect birer `sealed interface`'tir.

---

## 2. UiState

- `data class <Screen>UiState(...)` biçiminde, **tüm alanlar varsayılan değerli** tanımlanır.
- Immutable'dır; güncelleme yalnızca `copy(...)` ile yapılır.
- Ekranın gözlemlenebilir tüm durumunu temsil eder (single source of truth).
- **Türetilen alanlar** (örn. buton aktifliği) UI veya kullanıcı tarafından set edilmez;
  ViewModel tarafından diğer alanlardan türetilir (bkz. [mvi-viewmodel-rules.md](mvi-viewmodel-rules.md) §4).

```kotlin
data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginEnabled: Boolean = false, // türetilir; doğrudan set edilmez
)
```

---

## 3. Intent

- `sealed interface <Screen>Intent` biçiminde tanımlanır.
- Parametre taşıyan niyetler `data class`, parametresiz olanlar `data object`'tir.
- Yalnızca **kullanıcı niyetini** ifade eder; sonucu/iş mantığını değil
  (örn. `PasswordChanged`, `Submit` → `LoginSucceeded` DEĞİL).

```kotlin
sealed interface LoginIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data class PasswordChanged(val value: String) : LoginIntent
    data object TogglePasswordVisibility : LoginIntent
    data object Submit : LoginIntent
}
```

---

## 4. Effect

- `sealed interface <Screen>Effect` biçiminde tanımlanır.
- **Tek seferlik** (one-shot) olaylardır: navigasyon, snackbar, toast vb.
- **Yasak:** Effect'i UiState içinde boolean/flag olarak taşımak. Tekrar tetiklenme
  (configuration change) sorununa yol açar.
- Effect'ler ViewModel'de `Channel` ile üretilir, Route'ta `LaunchedEffect` ile tüketilir.

```kotlin
sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}
```

---

## 5. Sık Yapılan Hatalar

- State içinde `mutable` koleksiyon/alan tutmak (immutability bozulur).
- Effect yerine State'e `navigateNow: Boolean` gibi flag koymak.
- Intent'leri sonuç-odaklı isimlendirmek (`onLoginSuccess`) → niyet-odaklı olmalı (`Submit`).
- Türetilen alanı (`isLoginEnabled`) UI katmanında hesaplamak → ViewModel'in görevidir.

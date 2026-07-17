package com.turkcell.rencar_pair.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SPLASH_DURATION_MS = 3000L
private const val ROLE_PENDING = "PENDING"

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            // Token varligi tek basina yeterli degil: token PENDING iken alinmis
            // olabilir ve rol o zamandan beri sunucuda degismis olabilir (JWT'ye
            // gomulu rol claim'i token yenilenene kadar guncellenmez). Bu yuzden
            // soguk acilista GET /auth/me ile CANLI rol soruluyor. Cagri, splash
            // animasyonunun zaten var olan gecikmesiyle PARALEL baslatiliyor ki
            // ekstra bekleme suresi eklenmesin.
            val hasToken = tokenManager.getAccessToken() != null
            val roleDeferred = if (hasToken) {
                async { authRepository.getMe().getOrNull()?.role }
            } else {
                null
            }

            delay(SPLASH_DURATION_MS)
            _uiState.update { it.copy(isReady = true) }

            val destination = when {
                !hasToken -> SplashDestination.ONBOARDING
                roleDeferred?.await() == ROLE_PENDING -> SplashDestination.LICENSE
                // CUSTOMER/ADMIN, veya getMe() basarisiz oldu (ag hatasi): onceki
                // davranisa (Home) geri donuluyor. Token gercekten geçersizse zaten
                // getMe() cagrisi TokenAuthenticator uzerinden 401 alip
                // SessionManager.notifySessionExpired() tetikler ve NavHost bunu
                // ayrica yakalayip Login'e yonlendirir (bkz. RencarNavHost).
                else -> SplashDestination.HOME
            }
            _effect.send(SplashEffect.NavigateTo(destination))
        }
    }

    fun onIntent(intent: SplashIntent) {
        // Splash kullanıcı niyeti almaz; zamanlayıcı tabanlı otomatik geçiş yapar.
    }
}

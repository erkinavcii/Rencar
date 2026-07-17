package com.turkcell.rencar_pair.ui.auth.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.repository.AdminApprovalRepository
import com.turkcell.rencar_pair.data.repository.AuthRepository
import com.turkcell.rencar_pair.data.repository.LicenseRepository
import com.turkcell.rencar_pair.util.FaceMatcher
import com.turkcell.rencar_pair.util.FaceMatchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LicenseViewModel @Inject constructor(
    private val licenseRepository: LicenseRepository,
    private val faceMatcher: FaceMatcher,
    private val adminApprovalRepository: AdminApprovalRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LicenseUiState())
    val uiState: StateFlow<LicenseUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LicenseEffect>(Channel.BUFFERED)
    val effect: Flow<LicenseEffect> = _effect.receiveAsFlow()

    // AI onayi icin bellekte tutulan gorseller: sunucu selfie'yi geri vermedigi
    // (LicenseStatusResponseDto'da selfieImageUrl yok) icin upload anindaki byte'lar
    // saklanir. Surec (process) olurse (ekran yeniden acilirsa) kaybolur; bu durumda
    // "AI ile Anında Onayla" butonu kullanicidan tekrar dogrulama ister.
    private var cachedFrontBytes: ByteArray? = null
    private var cachedSelfieBytes: ByteArray? = null

    init {
        checkLicenseStatus()
    }

    fun onIntent(intent: LicenseIntent) {
        when (intent) {
            is LicenseIntent.FrontImageSelected -> updateState { it.copy(frontImageUri = intent.uri) }
            is LicenseIntent.BackImageSelected -> updateState { it.copy(backImageUri = intent.uri) }
            is LicenseIntent.SelfieImageSelected -> updateState { it.copy(selfieImageUri = intent.uri) }
            is LicenseIntent.NextStepClicked -> handleNextStep()
            is LicenseIntent.BackStepClicked -> handleBackStep()
            is LicenseIntent.Submit -> submit(intent.frontBytes, intent.backBytes, intent.selfieBytes)
            is LicenseIntent.RefreshStatus -> checkLicenseStatus()
            is LicenseIntent.MockBypassApprove -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(status = "APPROVED") }
                    _effect.send(LicenseEffect.NavigateToNext)
                }
            }
            is LicenseIntent.RequestAiApproval -> requestAiApproval()
        }
    }

    private fun updateState(transform: (LicenseUiState) -> LicenseUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            val isEnabled = when (updated.currentStep) {
                LicenseStep.EHLIYET -> updated.frontImageUri != null && updated.backImageUri != null
                LicenseStep.SELFIE -> updated.selfieImageUri != null
                LicenseStep.ONAY -> updated.status == "APPROVED"
            }
            updated.copy(isSubmitEnabled = isEnabled)
        }
    }

    private fun handleNextStep() {
        val current = _uiState.value
        when (current.currentStep) {
            LicenseStep.EHLIYET -> {
                if (current.frontImageUri != null && current.backImageUri != null) {
                    updateState { it.copy(currentStep = LicenseStep.SELFIE) }
                }
            }
            LicenseStep.SELFIE -> {
                // Submit intent should be dispatched instead because it requires byte arrays
            }
            LicenseStep.ONAY -> {
                if (current.status == "APPROVED") {
                    viewModelScope.launch {
                        _effect.send(LicenseEffect.NavigateToNext)
                    }
                }
            }
        }
    }

    private fun handleBackStep() {
        val current = _uiState.value
        when (current.currentStep) {
            LicenseStep.SELFIE -> updateState { it.copy(currentStep = LicenseStep.EHLIYET) }
            LicenseStep.ONAY -> {
                if (current.status == "REJECTED") {
                    // Reset everything to let them re-submit
                    updateState {
                        it.copy(
                            currentStep = LicenseStep.EHLIYET,
                            frontImageUri = null,
                            backImageUri = null,
                            selfieImageUri = null,
                            frontImageUrl = null,
                            backImageUrl = null,
                            status = "NOT_SUBMITTED",
                            rejectReason = null
                        )
                    }
                } else if (current.status != "APPROVED") {
                    updateState { it.copy(currentStep = LicenseStep.SELFIE) }
                }
            }
            LicenseStep.EHLIYET -> {
                // Handled in UI navigation back
            }
        }
    }

    private fun checkLicenseStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            licenseRepository.getLicenseStatus()
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            status = response.status,
                            frontImageUrl = response.frontImageUrl,
                            backImageUrl = response.backImageUrl,
                            rejectReason = response.rejectReason
                        )
                    }
                    if (response.status == "APPROVED" || response.status == "UNDER_REVIEW" || response.status == "REJECTED") {
                        updateState { it.copy(currentStep = LicenseStep.ONAY) }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(LicenseEffect.ShowError(error.message ?: "Durum sorgulanamadı."))
                }
        }
    }

    private fun submit(frontBytes: ByteArray, backBytes: ByteArray, selfieBytes: ByteArray) {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            licenseRepository.uploadLicense(frontBytes, backBytes, selfieBytes)
                .onSuccess { response ->
                    // "AI ile Anında Onayla" butonu icin sakla: sunucu selfie'yi status
                    // yanitinda geri vermiyor, bu yuzden byte'lar burada yakalanmali.
                    cachedFrontBytes = frontBytes
                    cachedSelfieBytes = selfieBytes
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            status = response.status,
                            frontImageUrl = response.frontImageUrl,
                            backImageUrl = response.backImageUrl,
                            rejectReason = response.rejectReason,
                            licenseId = response.id,
                            currentStep = LicenseStep.ONAY
                        )
                    }
                    updateState { it } // Re-evaluate submit button state
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(LicenseEffect.ShowError(error.message ?: "Ehliyet yükleme başarısız."))
                }
        }
    }

    // "AI ile Anında Onayla": on-device yuz eslestirme esigi gecilirse demo admin
    // hesabiyla anlik onay istenir; gecilmezse basvuru PENDING'de kalir ve normal
    // admin incelemesi beklenmeye devam eder (bu akis onu hicbir sekilde engellemez).
    private fun requestAiApproval() {
        val state = _uiState.value
        if (state.isAiApproving) return
        val licenseId = state.licenseId
        val frontBytes = cachedFrontBytes
        val selfieBytes = cachedSelfieBytes
        if (licenseId == null || frontBytes == null || selfieBytes == null) {
            viewModelScope.launch {
                _effect.send(
                    LicenseEffect.ShowError(
                        "AI onayı bu oturumda kullanılamıyor. Lütfen normal incelemeyi bekleyin.",
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiApproving = true) }

            when (val match = faceMatcher.match(frontBytes, selfieBytes)) {
                is FaceMatchResult.Success -> if (match.isMatch) {
                    adminApprovalRepository.approveViaAi(licenseId)
                        .onSuccess {
                            // Rol sunucuda CUSTOMER'a yukseltildi, ancak elimizdeki access
                            // token hala PENDING claim'i tasiyor (JWT'ye giris aninda gomulu).
                            // Kendi refresh token'imizla token'i yenilemezsek, Home'a gecince
                            // /vehicles ve /rentals/active cagrilari 403 (rol: PENDING) doner ve
                            // token dogal suresi dolup yenilenene kadar (TokenAuthenticator yalniz
                            // 401'de calisir, 403 onu tetiklemez) boyle kalir. best-effort: yenileme
                            // basarisiz olsa bile onaylanmis durumu asagida yine de gosteriyoruz.
                            authRepository.refreshSession()
                            _uiState.update { it.copy(isAiApproving = false) }
                            checkLicenseStatus() // gercek durumu sunucudan tazele
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(isAiApproving = false) }
                            _effect.send(
                                LicenseEffect.ShowError(error.message ?: "AI onayı başarısız oldu."),
                            )
                        }
                } else {
                    _uiState.update { it.copy(isAiApproving = false) }
                    val percent = (match.similarity * 100).toInt()
                    _effect.send(
                        LicenseEffect.ShowError(
                            "Yüzler yeterince eşleşmedi (benzerlik %$percent). Başvurunuz incelemeye devam ediyor.",
                        ),
                    )
                }
                FaceMatchResult.NoFaceInLicense -> {
                    _uiState.update { it.copy(isAiApproving = false) }
                    _effect.send(LicenseEffect.ShowError("Ehliyet ön yüzünde yüz tespit edilemedi."))
                }
                FaceMatchResult.NoFaceInSelfie -> {
                    _uiState.update { it.copy(isAiApproving = false) }
                    _effect.send(LicenseEffect.ShowError("Selfie görselinde yüz tespit edilemedi."))
                }
                is FaceMatchResult.Error -> {
                    _uiState.update { it.copy(isAiApproving = false) }
                    _effect.send(LicenseEffect.ShowError(match.message))
                }
            }
        }
    }
}

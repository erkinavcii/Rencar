package com.turkcell.rencar_pair.ui.auth.license

import android.net.Uri

enum class LicenseStep {
    EHLIYET,
    SELFIE,
    ONAY
}

data class LicenseUiState(
    val currentStep: LicenseStep = LicenseStep.EHLIYET,
    val frontImageUri: Uri? = null,
    val backImageUri: Uri? = null,
    val selfieImageUri: Uri? = null,
    val frontImageUrl: String? = null, // Web URL returned from getLicenseStatus
    val backImageUrl: String? = null,  // Web URL returned from getLicenseStatus
    val isLoading: Boolean = false,
    val isSubmitEnabled: Boolean = false, // True if active step requirements are met
    val status: String = "NOT_SUBMITTED", // NOT_SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED
    val rejectReason: String? = null, // Captured from backend status response
    val licenseId: String? = null, // upload yanitindan yakalanir; AI onayi bunu kullanir
    val isAiApproving: Boolean = false,
)

sealed interface LicenseIntent {
    data class FrontImageSelected(val uri: Uri) : LicenseIntent
    data class BackImageSelected(val uri: Uri) : LicenseIntent
    data class SelfieImageSelected(val uri: Uri) : LicenseIntent
    data class Submit(
        val frontBytes: ByteArray,
        val backBytes: ByteArray,
        val selfieBytes: ByteArray,
    ) : LicenseIntent
    data object NextStepClicked : LicenseIntent
    data object BackStepClicked : LicenseIntent
    data object RefreshStatus : LicenseIntent
    data object MockBypassApprove : LicenseIntent // Dummy bypass for demo presentation
    // Yuz eslestirme + admin onayini tetikler (yalniz UNDER_REVIEW durumunda gosterilir).
    data object RequestAiApproval : LicenseIntent
    // Soguk acilista PENDING kullanici dogrudan bu ekrana dustugunde backstack bos
    // kalabiliyor (geri oku hicbir sey yapmiyor); kullanicinin sikismadan cikabilmesi
    // icin eklendi.
    data object Logout : LicenseIntent
}

sealed interface LicenseEffect {
    data class ShowError(val message: String) : LicenseEffect
    data object NavigateToNext : LicenseEffect
    data object NavigateToOnboarding : LicenseEffect
}

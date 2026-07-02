package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.AdminLicenseResponseDto
import com.turkcell.rencar_pair.data.model.LicenseResponseDto
import com.turkcell.rencar_pair.data.model.LicenseStatusResponseDto

interface LicenseRepository {
    suspend fun uploadLicense(frontBytes: ByteArray, backBytes: ByteArray): Result<LicenseResponseDto>
    suspend fun getLicenseStatus(): Result<LicenseStatusResponseDto>
    suspend fun adminApproveLicense(id: String): Result<AdminLicenseResponseDto>
    suspend fun getMyLicenseId(): Result<String>
}

package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.domain.model.Quote
import com.turkcell.rencar_pair.domain.model.Vehicle

interface VehicleRepository {
    suspend fun getAvailableVehicles(page: Int = 1, limit: Int = 20): Result<List<Vehicle>>
    suspend fun getVehicleDetails(id: String): Result<Vehicle>
    suspend fun getQuote(id: String, plan: String, minutes: Int): Result<Quote>
}
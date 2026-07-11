package com.turkcell.rencar_pair.data.history

interface HistoryRepository {
    suspend fun getHistory(): Result<HistorySummary>
}
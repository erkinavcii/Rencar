package com.turkcell.rencar_pair.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.history.HistoryRepository
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
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HistoryEffect>(Channel.BUFFERED)
    val effect: Flow<HistoryEffect> = _effect.receiveAsFlow()

    init {
        onIntent(HistoryIntent.LoadHistory)
    }

    fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.LoadHistory -> loadHistory()
            is HistoryIntent.Refresh -> loadHistory()
            is HistoryIntent.SortToggled -> toggleSort()
            is HistoryIntent.SearchQueryChanged ->
                _uiState.update { it.copy(searchQuery = intent.query) }
            is HistoryIntent.MonthFilterChanged ->
                _uiState.update { it.copy(selectedMonthFilter = intent.monthKey) }
            is HistoryIntent.TripSelected ->
                _uiState.update { it.copy(selectedTripId = intent.tripId) }
            is HistoryIntent.TripDetailDismissed ->
                _uiState.update { it.copy(selectedTripId = null) }
        }
    }

    private fun toggleSort() {
        _uiState.update {
            val next = if (it.selectedSort == HistorySortOption.DATE_DESC) {
                HistorySortOption.DATE_ASC
            } else {
                HistorySortOption.DATE_DESC
            }
            it.copy(selectedSort = next)
        }
    }

    private fun loadHistory() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            historyRepository.getHistory()
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            monthlyTripCount = summary.monthlyTripCount,
                            monthlySpending = summary.monthlySpending,
                            totalTripCount = summary.totalTripCount,
                            totalSpending = summary.totalSpending,
                            trips = summary.trips,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        HistoryEffect.ShowMessage(
                            throwable.message ?: "Geçmiş yüklenemedi."
                        )
                    )
                }
        }
    }
}
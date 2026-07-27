package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.PricesResponse
import com.example.data.repository.PriceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState {
    object Loading : UiState
    data class Success(val response: PricesResponse) : UiState
    data class Error(val message: String) : UiState
}

class PriceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PriceRepository(application)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Expose the cache flow as the UI state stream
    val uiState: StateFlow<UiState> = repository.getCachedPricesFlow()
        .map { response ->
            if (response.items.isEmpty() || response.items.all { it.currentPrice == "نامشخص" }) {
                UiState.Loading
            } else {
                UiState.Success(response)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    init {
        // Initial fetch on viewmodel creation
        refresh()
        startForegroundPolling()
    }

    private var pollingJob: kotlinx.coroutines.Job? = null

    private fun startForegroundPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60000L)
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchFreshPrices()
            _isRefreshing.value = false
        }
    }
}

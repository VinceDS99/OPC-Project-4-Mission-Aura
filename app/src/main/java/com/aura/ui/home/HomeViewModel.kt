package com.aura.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ui.home.model.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadAccounts(userId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(3000) // simulation du délai pour voir la progress bar

            when (val result = HomeRepository.getAccounts(userId)) {
                is HomeResult.Success -> {
                    val mainAccount = result.accounts.firstOrNull { it.main }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            balance = mainAccount?.balance?.toString() ?: "0.0",
                            errorMessage = null
                        )
                    }
                }
                is HomeResult.NoConnection -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Pas de connexion internet")
                    }
                }
                is HomeResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Impossible de récupérer les comptes")
                    }
                }
            }
        }
    }
}

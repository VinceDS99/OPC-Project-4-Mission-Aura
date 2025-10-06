package com.aura.ui.home.model

data class HomeUiState(
    val balance: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasShownError: Boolean = false
)

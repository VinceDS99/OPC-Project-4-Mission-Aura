package com.aura.ui.transfer

data class TransferUiState(
    val recipient: String = "",
    val amount: String = "",
    val isTransferButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val success: Boolean? = null,
    val errorMessage: String? = null
)

package com.aura.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransferViewModel(
    private val repository: TransferRepository = TransferRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState

    fun onRecipientChanged(value: String) {
        _uiState.update {
            it.copy(
                recipient = value,
                isTransferButtonEnabled = value.isNotBlank() && it.amount.isNotBlank() && !it.isLoading,
                errorMessage = null,
                success = null
            )
        }
    }

    fun onAmountChanged(value: String) {
        _uiState.update {
            it.copy(
                amount = value,
                isTransferButtonEnabled = it.recipient.isNotBlank() && value.isNotBlank() && !it.isLoading,
                errorMessage = null,
                success = null
            )
        }
    }

    fun transfer(senderId: String, userBalance: Double) {
        val current = _uiState.value
        val recipient = current.recipient.trim()
        val amountDouble = current.amount.toDoubleOrNull() ?: -1.0

        // Vérifications locales avant l'appel API
        when {
            recipient == senderId -> {
                _uiState.update {
                    it.copy(
                        errorMessage = "Vous ne pouvez pas transférer vers votre propre compte",
                        success = false
                    )
                }
                return
            }
            amountDouble <= 0.0 -> {
                _uiState.update {
                    it.copy(
                        errorMessage = "Montant invalide (doit être supérieur à 0)",
                        success = false
                    )
                }
                return
            }
            amountDouble > userBalance -> {
                _uiState.update {
                    it.copy(
                        errorMessage = "Solde insuffisant pour effectuer ce transfert",
                        success = false
                    )
                }
                return
            }
        }

        // Appel au backend
        _uiState.update { it.copy(isLoading = true, errorMessage = null, success = null) }

        viewModelScope.launch {
            delay(500) // effet visuel progress bar
            val result = repository.makeTransfer(senderId, recipient, amountDouble)

            _uiState.update {
                when (result) {
                    is TransferResult.Success -> it.copy(isLoading = false, success = true, errorMessage = null)
                    is TransferResult.NoConnection -> it.copy(isLoading = false, success = false, errorMessage = "Pas de connexion Internet")
                    is TransferResult.InvalidRecipient -> it.copy(isLoading = false, success = false, errorMessage = "Destinataire invalide")
                    is TransferResult.InsufficientFunds -> it.copy(isLoading = false, success = false, errorMessage = "Solde insuffisant pour effectuer ce transfert")
                    is TransferResult.InvalidAmount -> it.copy(isLoading = false, success = false, errorMessage = "Montant invalide (doit être supérieur à 0)")
                    else -> it.copy(isLoading = false, success = false, errorMessage = "Erreur du serveur, veuillez réessayer plus tard")
                }
            }
        }
    }
}

package com.aura.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.model.login.Credentials
import com.aura.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val id: String = "",
    val password: String = "",
    val isButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean? = null
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onIdChanged(newValue: String) {
        _uiState.update { current ->
            current.copy(
                id = newValue,
                isButtonEnabled = newValue.isNotBlank() && current.password.isNotBlank(),
                loginSuccess = null // reset lors de la saisie
            )
        }
    }

    fun onPasswordChanged(newValue: String) {
        _uiState.update { current ->
            current.copy(
                password = newValue,
                isButtonEnabled = newValue.isNotBlank() && current.id.isNotBlank(),
                loginSuccess = null // reset lors de la saisie
            )
        }
    }

    fun login() {
        val credentials = Credentials(
            id = _uiState.value.id,
            password = _uiState.value.password
        )

        _uiState.update { it.copy(isLoading = true, loginSuccess = null) }

        viewModelScope.launch {
            val success = LoginRepository.login(credentials)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    loginSuccess = success
                )
            }
        }
    }
}

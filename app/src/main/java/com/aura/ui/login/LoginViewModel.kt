package com.aura.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.model.login.Credentials
import com.aura.repository.LoginRepository
import com.aura.repository.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class LoginUiState(
    val id: String = "",
    val password: String = "",
    val isButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean? = null,
    val errorMessage: String? = null // nouveau champ pour message spécifique
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onIdChanged(newValue: String) {
        _uiState.update { current ->
            current.copy(
                id = newValue,
                isButtonEnabled = newValue.isNotBlank() && current.password.isNotBlank(),
                loginSuccess = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(newValue: String) {
        _uiState.update { current ->
            current.copy(
                password = newValue,
                isButtonEnabled = newValue.isNotBlank() && current.id.isNotBlank(),
                loginSuccess = null,
                errorMessage = null
            )
        }
    }

    fun login() {
        val credentials = Credentials(
            id = _uiState.value.id,
            password = _uiState.value.password
        )

        _uiState.update { it.copy(isLoading = true, loginSuccess = null, errorMessage = null) }

        viewModelScope.launch {
            // 🔹 Simulation d'un délai pour l'affichage de la ProgressBar au login
            //delay(2000)
            val result = LoginRepository.login(credentials)

            _uiState.update {
                when(result) {
                    LoginResult.Success -> it.copy(isLoading = false, loginSuccess = true)
                    LoginResult.Failure -> it.copy(isLoading = false, loginSuccess = false, errorMessage = "Identifiants incorrect")
                    LoginResult.NoConnection -> it.copy(isLoading = false, loginSuccess = false, errorMessage = "Pas de connexion internet")
                }
            }
        }
    }
}

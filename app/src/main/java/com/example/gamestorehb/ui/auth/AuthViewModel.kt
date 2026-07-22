package com.example.gamestorehb.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestorehb.domain.model.AuthResult
import com.example.gamestorehb.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val successUsername: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (!validateInput(username, password)) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.login(username.trim(), password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(isSuccess = true, successUsername = result.username)
                is AuthResult.Error   -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun register(username: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _uiState.value = AuthUiState(errorMessage = "Password tidak cocok")
            return
        }
        if (!validateInput(username, password)) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.register(username.trim(), password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(isSuccess = true, successUsername = result.username)
                is AuthResult.Error   -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validateInput(username: String, password: String): Boolean {
        return when {
            username.isBlank() -> { _uiState.value = AuthUiState(errorMessage = "Username tidak boleh kosong"); false }
            username.trim().length < 3 -> { _uiState.value = AuthUiState(errorMessage = "Username minimal 3 karakter"); false }
            password.isBlank() -> { _uiState.value = AuthUiState(errorMessage = "Password tidak boleh kosong"); false }
            password.length < 6 -> { _uiState.value = AuthUiState(errorMessage = "Password minimal 6 karakter"); false }
            else -> true
        }
    }
}

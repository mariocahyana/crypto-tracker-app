package com.example.gamestorehb.util

/**
 * A sealed class representing all possible UI states for any data-loading operation.
 * Drives strict, type-safe state management in ViewModels via StateFlow.
 *
 * @param T The type of data wrapped in the Success state.
 */
sealed class UiState<out T> {
    /** Initial/in-progress loading state. */
    data object Loading : UiState<Nothing>()

    /** Successfully loaded data. */
    data class Success<T>(val data: T) : UiState<T>()

    /** Data loaded but list is empty. */
    data object Empty : UiState<Nothing>()

    /** An error occurred. Contains the human-readable message. */
    data class Error(val message: String) : UiState<Nothing>()
}

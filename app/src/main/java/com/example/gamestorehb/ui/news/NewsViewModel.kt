package com.example.gamestorehb.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestorehb.domain.model.NewsArticle
import com.example.gamestorehb.domain.usecase.GetNewsUseCase
import com.example.gamestorehb.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase
) : ViewModel() {

    private val _newsState = MutableStateFlow<UiState<List<NewsArticle>>>(UiState.Loading)
    val newsState: StateFlow<UiState<List<NewsArticle>>> = _newsState.asStateFlow()

    init {
        fetchNews()
    }

    fun fetchNews() {
        viewModelScope.launch {
            _newsState.value = UiState.Loading
            getNewsUseCase()
                .catch { error ->
                    _newsState.value = UiState.Error(error.message ?: "Failed to fetch news")
                }
                .collect { articles ->
                    if (articles.isEmpty()) {
                        _newsState.value = UiState.Empty
                    } else {
                        _newsState.value = UiState.Success(articles)
                    }
                }
        }
    }
}

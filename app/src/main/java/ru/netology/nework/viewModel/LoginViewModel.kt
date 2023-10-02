package ru.netology.nework.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.model.AuthModelState
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth,
) : ViewModel() {
    private val _authorized = SingleLiveEvent<AuthModelState>()
    val authorized: LiveData<AuthModelState>
        get() = _authorized

    suspend fun authorization(login: String, pass: String) {
        viewModelScope.launch {
            _authorized.value = AuthModelState(authLoading = true)
            try {
                val response = apiService.authorization(login, pass).body()
                response?.token?.let { appAuth.setAuth(response.id, response.token, null) }
                _authorized.value = AuthModelState(authSuccessful = true)
            } catch (e: Exception) {
                _authorized.value = AuthModelState(authError = true)
            }
        }
    }
}
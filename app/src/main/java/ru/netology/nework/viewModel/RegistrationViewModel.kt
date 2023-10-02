package ru.netology.nework.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Token
import ru.netology.nework.model.AuthModelState
import ru.netology.nework.model.MediaModel
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth,
) : ViewModel() {

    private val _registered = SingleLiveEvent<AuthModelState>()
    val registered: LiveData<AuthModelState>
        get() = _registered

    private val _avatar = MutableLiveData(MediaModel())
    val avatar: LiveData<MediaModel>
        get() = _avatar

    fun changePhoto(uri: Uri?, file: File?) {
        _avatar.value = MediaModel(uri, file)
    }

    suspend fun registration(login: String, pass: String, name: String, upload: MediaUpload?) {
        viewModelScope.launch {
            val token: Token
            _registered.value = AuthModelState(authLoading = true)

            try {
                val response = if (upload != null) {
                    apiService.registerWithPhoto(
                        login.toRequestBody("text/plain".toMediaType()),
                        pass.toRequestBody("text/plain".toMediaType()),
                        name.toRequestBody("text/plain".toMediaType()),
                        MultipartBody.Part.createFormData(
                            "file", upload.file.name, upload.file.asRequestBody()
                        )
                    )
                } else {
                    apiService.register(login, pass, name)
                }

                if (!response.isSuccessful) {
                    _registered.value = AuthModelState(authError = true)
                } else {
                    token = response.body() ?: Token(id = 0, token = "")
                    appAuth.setAuth(token.id, token.token, null)
                    _registered.value = AuthModelState(authSuccessful = true)
                }
            } catch (e: Exception) {
                _registered.value = AuthModelState(authError = true)
            }
        }
    }
}
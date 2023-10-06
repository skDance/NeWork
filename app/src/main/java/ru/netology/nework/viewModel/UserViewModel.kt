package ru.netology.nework.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.User
import ru.netology.nework.model.UserModel
import ru.netology.nework.model.UserModelState
import ru.netology.nework.repository.Repository
import javax.inject.Inject

val emptyUser = User(
    id = -1,
    login = "",
    name = "",
    avatar = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val appAuth: AppAuth
) : AndroidViewModel(application) {
    var data: Flow<UserModel> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->
        repository.userData()
            .map { user ->
                UserModel(
                    user,
                    user.isEmpty()
                )
            }
    }

    private val _state = MutableLiveData(UserModelState())
    val state: LiveData<UserModelState>
        get() = _state

    val openProfile: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    init {
        getUsers()
    }

    fun getUsers() = viewModelScope.launch  {
        try {
            _state.value = UserModelState(loading = true)
            repository.getUsers()
            _state.value = UserModelState()
        } catch (_: Exception) {
            _state.value = UserModelState(error = true)
        }
    }

    fun refreshUsers() = viewModelScope.launch  {
        try {
            _state.value = UserModelState(refreshing = true)
            repository.getUsers()
        } catch (_: Exception) {
            _state.value = UserModelState(error = true)
        }
    }

}
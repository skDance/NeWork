package ru.netology.nework.viewModel

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.model.FeedModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.MediaModel
import ru.netology.nework.repository.Repository
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

val emptyPost = Post(
    id = 0,
    authorId = 0,
    content = "",
    author = "",
    likeOwnerIds = emptyList(),
    mentionIds = emptyList(),
    published = ""
)

@HiltViewModel
class PostViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val appAuth: AppAuth
) : AndroidViewModel(application) {

    val data: LiveData<FeedModel> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->
        repository.postData()
            .map { posts ->
                FeedModel(
                    posts.map { post -> post.copy(ownedByMe = post.authorId == myId) },
                    posts.isEmpty()
                )
            }
    }.asLiveData(Dispatchers.Default)

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    private val edited = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val openPicture: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val openPostById: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }

    private val _media = MutableLiveData(
        MediaModel(
            edited.value?.attachment?.url?.toUri(),
            edited.value?.attachment?.url?.toUri()?.toFile(),
            edited.value?.attachment?.type
        )
    )
    val media: LiveData<MediaModel>
        get() = _media

//    val newerCount = repository.getNewerCount()
//        .catch { e -> e.printStackTrace() }
//        .asLiveData(Dispatchers.Default)


    fun changeMedia(uri: Uri?, file: File?, attachmentType: AttachmentType?) {
        _media.value = MediaModel(uri, file, attachmentType)
    }


    init {
        loadPosts()
    }

//    fun viewNewPosts() = viewModelScope.launch {
//        try {
//            repository.showNewPosts()
//            _state.value = FeedModelState(loading = true)
//        } catch (e: Exception) {
//            _state.value = FeedModelState(error = true)
//        }
//    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            repository.getAllAsync()
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(refreshing = true)
            repository.getAllAsync()
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    fun shareById(id: Long) = repository.shareById(id)

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                repository.likeByIdAsync(post)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeByIdAsync(id)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun getEditedId(): Long {
        return edited.value?.id ?: 0
    }

    fun getEditedPostAttachment(): Attachment? {
        return edited.value?.attachment
    }

    fun changeContent(content: String) {
        val text = content.trim()

        if (edited.value?.content == text) return
        edited.value = edited.value?.copy(content = text)
    }

    fun deleteAttachment() {
        edited.value = edited.value?.copy(attachment = null)
    }

    fun save() {
        edited.value?.let { savingPost ->
            _postCreated.value = Unit

            viewModelScope.launch {
                try {
                    when (_media.value) {
                        MediaModel() -> repository.saveAsync(savingPost)
                        else -> _media.value?.file?.let { file ->
                            repository.saveWithAttachment(
                                savingPost,
                                MediaUpload(file),
                                _media.value!!.attachmentType!!
                            )
                        }
                    }

                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }

        edited.value = emptyPost
        _media.value = MediaModel()
    }
}
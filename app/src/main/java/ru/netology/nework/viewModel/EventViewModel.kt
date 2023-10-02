package ru.netology.nework.viewModel

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
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
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventType
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.User
import ru.netology.nework.model.EventModel
import ru.netology.nework.model.EventModelState
import ru.netology.nework.model.MediaModel
import ru.netology.nework.repository.Repository
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

val emptyEvent = Event(
    id = 0,
    authorId = 0,
    content = "",
    author = "",
    likeOwnerIds = emptyList(),
    datetime = "",
    type = EventType.OFFLINE,
    published = "",
)

@HiltViewModel
class EventViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val appAuth: AppAuth,
) : AndroidViewModel(application) {

    val data: Flow<EventModel> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->
        repository.eventData()
            .map { event ->
                EventModel(
                    event.map { event -> event.copy(ownedByMe = event.authorId == myId)},
                    event.isEmpty()
                )
            }
    }

    private val _participants1 = SingleLiveEvent<List<User>>()
    val participants1: LiveData<List<User>>
        get() = _participants1

    var participants: List<User> = emptyList()

    val openEventDialogId: MutableLiveData<Event> by lazy {
        MutableLiveData<Event>()
    }
    val openEventDialogType: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val _state = MutableLiveData(EventModelState())
    val state: LiveData<EventModelState>
        get() = _state

    private val edited = MutableLiveData(emptyEvent)
    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _media = MutableLiveData(
        MediaModel(
            edited.value?.attachment?.url?.toUri(),
            edited.value?.attachment?.url?.toUri()?.toFile(),
            edited.value?.attachment?.type
        )
    )

    val media: LiveData<MediaModel>
        get() = _media

    fun changeMedia(uri: Uri?, file: File?, attachmentType: AttachmentType?) {
        _media.value = MediaModel(uri, file, attachmentType)
    }

    init {
        loadEvents()
    }

    fun loadEvents() = viewModelScope.launch {
        try {
            _state.value = EventModelState(loading = true)
            repository.getAllEvents()
            _state.value = EventModelState()
        } catch (_: Exception) {
            _state.value = EventModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _state.value = EventModelState(refreshing = true)
            repository.getAllEvents()
            _state.value = EventModelState()
        } catch (_: Exception) {
            _state.value = EventModelState(error = true)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeEventsById(id)
        } catch (_: Exception) {
            _state.value = EventModelState(error = true)
        }
    }

    fun edit(event: Event) {
        edited.value = event
    }

    fun getEditedId(): Long {
        return edited.value?.id ?: 0
    }

    fun getEditedEventAttachment(): Attachment? {
        return edited.value?.attachment
    }

    fun changeContent(
        content: String,
        datetime: String,
        type: EventType,
        speakerIds: List<Long>
    ) {
        val text = content.trim()

        if (edited.value?.content == text && edited.value?.datetime == datetime && edited.value?.type == type && edited.value?.speakerIds == speakerIds)
            return
        edited.value = edited.value?.copy(
            content = text,
            datetime = datetime,
            type = type,
            speakerIds = speakerIds
        )
    }

    fun deleteAttachment() {
        edited.value = edited.value?.copy(attachment = null)
    }

    fun joinById(event: Event) = viewModelScope.launch {
        try {
            repository.joinByIdEvents(event)
            _state.value = EventModelState()
        } catch (e: Exception) {
            _state.value = EventModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let { savingEvents ->
            _eventCreated.value = Unit

            viewModelScope.launch {
                try {
                    when (_media.value) {
                        MediaModel() -> repository.saveEvents(savingEvents)
                        else -> _media.value?.file?.let { file ->
                            repository.saveEventsWithAttachment(
                                savingEvents,
                                MediaUpload(file),
                                _media.value!!.attachmentType!!
                            )
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }

        edited.value = emptyEvent
        _media.value = MediaModel()
    }

    fun getParticipants(event: Event) = viewModelScope.launch {
        try {
            _state.value = EventModelState(loading = true)
            _participants1.value =  repository.getParticipants(event.participantsIds)
            _state.value = EventModelState()
        } catch (e: Exception) {
            _state.value = EventModelState(error = true)
        }
    }
}
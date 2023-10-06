package ru.netology.nework.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.User
import ru.netology.nework.model.EventModel
import ru.netology.nework.model.EventModelState
import ru.netology.nework.model.JobModel
import ru.netology.nework.model.JobModelState
import ru.netology.nework.repository.Repository
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

val emptyJob = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
    finish = "",
    link = "",
)

@HiltViewModel
class JobViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val appAuth: AppAuth
) : AndroidViewModel(application) {

    val data: Flow<JobModel> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->
        repository.jobData()
            .map { jobs ->
                JobModel(jobs, jobs.isEmpty())
            }
    }

    private val edited = MutableLiveData(emptyJob)

    val editedJob: MutableLiveData<Job> by lazy {
        MutableLiveData<Job>()
    }

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    private val jobEdit = MutableLiveData(emptyJob)

    private val _state = MutableLiveData(JobModelState())
    val state: LiveData<JobModelState>
        get() = _state

    private val _jobsList = SingleLiveEvent<List<Job>>()
    val jobsList: LiveData<List<Job>>
        get() = _jobsList

    init {
        loadMyJobs()
    }

    fun loadMyJobs() = viewModelScope.launch {
        try {
            _jobsList.value = repository.getMyJobs(appAuth.authStateFlow.value.id)
        } catch (_: Exception) {
        }
    }

    fun loadUserJobs(id: Long) = viewModelScope.launch {
        try {
            repository.getJobs(id)
        } catch (_: Exception) {
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeJobById(id)
        } catch (_: Exception) {
        }
    }

    fun changeContent(
        name: String,
        position: String,
        start: String,
        finish: String?,
        link: String?,
        ) {
        if (edited.value?.name == name
            && edited.value?.position == position
            && edited.value?.start == start
            && edited.value?.finish == finish
            && edited.value?.link == link
        ) return

        edited.value = edited.value?.copy(
            name = name,
            position = position,
            start = start,
            finish = finish,
            link = link
        )
    }

    fun save() {
        _jobCreated.value = Unit
        viewModelScope.launch {
            try {
                edited.value?.let {
                    repository.saveJob(it)
                }
            } catch (_: Exception) {
                _state.value = JobModelState(error = true)
            }
        }
        edited.value = emptyJob
    }

    fun edit(job: Job) {
         editedJob.value = job
    }
}
package ru.netology.nework.model

import ru.netology.nework.dto.Job
import ru.netology.nework.dto.User

data class JobModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
)

data class JobModel(
    val jobs: List<Job>? = emptyList(),
    val empty: Boolean = false,
)
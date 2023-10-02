package ru.netology.nework.model

import ru.netology.nework.dto.Event

data class EventModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
)

data class EventModel(
    val events: List<Event>? = emptyList(),
    val empty: Boolean = false,
)
package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User

interface Repository {
    fun postData(): Flow<List<Post>>
    fun eventData(): Flow<List<Event>>
    fun userData(): Flow<List<User>>


    suspend fun getAllAsync()
    suspend fun removeByIdAsync(id: Long)
    suspend fun saveAsync(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload, attachmentType: AttachmentType)
    fun shareById(id: Long)
    suspend fun likeByIdAsync(post: Post)
    suspend fun upload(upload: MediaUpload): MediaResponse
    suspend fun getAllEvents()
    suspend fun saveEvents(event: Event)
    suspend fun saveEventsWithAttachment(event: Event, upload: MediaUpload, attachmentType: AttachmentType)
    suspend fun removeEventsById(id: Long)
    suspend fun joinByIdEvents(event: Event)
    suspend fun getUsers()
    suspend fun getUserBuId(id: Long)
    suspend fun getParticipants(participantsId: List<Long>): List<User>
}
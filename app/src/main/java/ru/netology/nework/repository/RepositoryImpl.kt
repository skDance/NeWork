package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventCreateRequest
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.entity.EventsEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.UserEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val eventDao: EventDao,
    private val userDao: UserDao,
    private val apiService: ApiService
) : Repository {

    override fun postData(): Flow<List<Post>> = postDao.getAll().map { it.map(PostEntity::toDto) }
    override fun eventData(): Flow<List<Event>> =
        eventDao.getAllEvents().map { it.map(EventsEntity::toDto) }

    override fun userData(): Flow<List<User>> = userDao.getUsers().map { it.map(UserEntity::toDto) }

    override suspend fun getAllAsync() {
        val response = apiService.getAll()

        if (!response.isSuccessful) throw java.lang.RuntimeException("api error")
        response.body() ?: throw java.lang.RuntimeException("body is null")
        postDao.insert(response.body()!!.map { PostEntity.fromDto(it) })
    }

    override suspend fun removeByIdAsync(id: Long) {
        try {
            val response = apiService.removeById(id)

            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            postDao.removeById(id)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveAsync(post: Post) {
        try {
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code().toString())
            }

            val body = response.body() ?: throw ApiError(response.code().toString())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(
        post: Post,
        upload: MediaUpload,
        attachmentType: AttachmentType
    ) {
        try {
            val media = upload(upload)

            val postWithAttachment = post.copy(
                attachment =
                Attachment(
                    url = media.url,
                    type = attachmentType,
                )
            )

            saveAsync(postWithAttachment)

        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun likeByIdAsync(post: Post) {
        postDao.likeById(post.id)
        try {
            val response = if (post.likedByMe) {
                apiService.dislikeById(post.id)
            } else {
                apiService.likeById(post.id)
            }
            if (!response.isSuccessful) throw java.lang.RuntimeException("api error")
            val body = response.body() ?: throw java.lang.RuntimeException("body is null")
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): MediaResponse {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)

            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            return response.body() ?: throw ApiError(response.message())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getAllEvents() {
        try {
            val response = apiService.getAllEvents()

            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            val body = response.body() ?: throw ApiError(response.message())

            eventDao.insertEvents(body.toEntity())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEvents(event: Event) {
        try {
            val eventRequest = EventCreateRequest(
                event.id,
                event.content,
                event.datetime,
                event.type,
                event.attachment,
                event.link,
                event.speakerIds
            )

            val response = apiService.saveEvents(eventRequest)

            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            val body = response.body() ?: throw ApiError(response.message())

            eventDao.saveEvent(EventsEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEventsWithAttachment(
        event: Event,
        upload: MediaUpload,
        attachmentType: AttachmentType
    ) {
        try {
            val media = upload(upload)

            val eventWithAttachment = event.copy(
                attachment =
                Attachment(
                    url = media.url,
                    type = attachmentType,
                )
            )

            saveEvents(eventWithAttachment)

        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeEventsById(id: Long) {
        try {
            val response = apiService.removeByIdEvent(id)

            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            eventDao.removeByIdEvent(id)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun joinByIdEvents(event: Event) {
        eventDao.joinByIdEvent(event.id)
        try {
            val response = if (event.participatedByMe) {
                apiService.unJoinByIdEvent(event.id)
            } else {
                apiService.joinByIdEvent(event.id)
            }

            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            val body = response.body() ?: throw ApiError(response.message())

            eventDao.insertEvents(EventsEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getUsers() {
        try {
            val response = apiService.getUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            val users = body.map {
                User(
                    id = it.id,
                    login = it.login,
                    name = it.name,
                    avatar = it.avatar
                )
            }
            userDao.insertUsers(users.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getUserBuId(id: Long) {
        try {
            val response = apiService.getUserById(id)

            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            val body = response.body() ?: throw ApiError(response.message())
            val userDaoSaved = User(
                id = body.id,
                login = body.login,
                name = body.name,
                avatar = body.avatar,
            )

            userDao.insertUsers(UserEntity.fromDto(userDaoSaved))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getParticipants(participantsId: List<Long>): List<User> {
        var participantsList: List<User> = emptyList()
        participantsId.forEach {
            val response = apiService.getUserById(it)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            participantsList += User(body.id, body.login, body.name, body.avatar)
        }
        return participantsList
    }
}
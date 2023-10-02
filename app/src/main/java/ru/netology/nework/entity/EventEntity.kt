package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventType

@Entity
class EventsEntity(
    @PrimaryKey
    val id:Long,
    val authorId:Long,
    val author:String,
    val authorAvatar:String? = null,
    val authorJob:String? = null,
    val content:String,
    val datetime:String,
    val published:String,
    val typeEvent: EventType,
    val likeOwnerIds:  String?,
    val likedByMe:Boolean = false,
    val speakerIds:  String?,
    val participantsIds:  String?,
    val participatedByMe:Boolean = false,

    @Embedded
    val attachment: Attachment? = null,
    val link:String? = null,
    val ownedByMe:Boolean = false,

    )  {

    fun toDto() = Event(
        id, authorId, author, authorAvatar, authorJob, content, datetime, published, typeEvent, toListDto(likeOwnerIds),
        likedByMe, toListDto(speakerIds), toListDto(participantsIds), participatedByMe, attachment, link, ownedByMe, null
    )

    companion object {
        fun fromDto(dto: Event) =
            EventsEntity(
                dto.id, dto.authorId, dto.author, dto.authorAvatar, dto.authorJob, dto.content, dto.datetime, dto.published, dto.type, fromListDto(dto.likeOwnerIds),
                dto.likedByMe, fromListDto(dto.speakerIds), fromListDto(dto.participantsIds), dto.participatedByMe, dto.attachment, dto.link, dto.ownedByMe
            )
    }
}

fun List<EventsEntity>.toDto(): List<Event> = map(EventsEntity::toDto)
fun List<Event>.toEntity(): List<EventsEntity> = map(EventsEntity::fromDto)
package ru.netology.nework.dto

data class Event(
    val id:Long,
    val authorId:Long,
    val author:String,
    val authorAvatar:String? = null,
    val authorJob:String? = null,
    val content:String,
    val datetime:String,
    val published:String,
    val type:EventType,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe:Boolean = false,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participatedByMe:Boolean = false,
    val attachment:Attachment? = null,
    val link:String? = null,
    val ownedByMe:Boolean = false,
    val users: Map<String, UserPreview>? = null
)

data class EventCreateRequest(
    val id: Long,
    val content:String,
    val datetime:String,
    val type: EventType? = null,
    val attachment: Attachment? = null,
    val link:String? = null,
    val speakerIds:List<Long>? = null
)

enum class EventType {
    OFFLINE, ONLINE
}
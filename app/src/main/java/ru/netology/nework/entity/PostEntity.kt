package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    val link: String? = null,
    val likeOwnerIds: String?,
    val likedByMe: Boolean = false,
    val mentionIds: String?,
    val mentionedMe: Boolean = false,
    @Embedded
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    var likesCount: Int = 0,
) {

    init {
        likesCount = toListDto(likeOwnerIds).size
    }

    fun toDto() = Post(
        id, authorId, author, authorAvatar, content, published, link, toListDto(likeOwnerIds),
        likedByMe, toListDto(mentionIds), mentionedMe, attachment, ownedByMe,
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.link,
                fromListDto(dto.likeOwnerIds),
                dto.likedByMe,
                fromListDto(dto.mentionIds),
                dto.mentionedMe,
                dto.attachment,
                dto.ownedByMe,
            )
    }

}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(isNew: Boolean = false): List<PostEntity> = map(PostEntity::fromDto)

fun fromListDto(list: List<Long>?): String {
    if (list == null) return ""
    return list.toString()
}

fun toListDto(data: String?): List<Long> {
    return if (data == "[]") emptyList()
    else {
        val substr = data?.substring(1, data.length - 1)
        substr?.split(", ")?.map { it.toLong() } ?: emptyList()
    }
}
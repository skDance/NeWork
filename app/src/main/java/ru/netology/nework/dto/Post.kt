package ru.netology.nework.dto

sealed interface FeedItem {
    val id: Long
}

data class Post (
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    val link: String? = null,
    val likeOwnerIds: List<Long>? = emptyList(),
    val likedByMe: Boolean = false,
    val mentionIds: List<Long>? = emptyList(),
    val mentionedMe: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val likesCount: Int = 0,
): FeedItem {

    fun setCount(count: Int): String {
        val stringCount = count.toString()
        val countArray = stringCount.toCharArray()
        when {
            count in 0..999 -> return "$count"
            count in 1000..9999 -> {
                return if (countArray[1].equals('0')) "${countArray[0]}K" else "${countArray[0]}.${countArray[1]}K"
            }

            count in 10000..99_999 -> return "${countArray[0]}${countArray[1]}K"
            count in 100_000..999_999 -> return "${countArray[0]}${countArray[1]}${countArray[2]}K"
            count in 1_000_000..9_000_000 -> {
                return if (countArray[1].equals('0')) "${countArray[0]}M" else "${countArray[0]}.${countArray[1]}M"
            }

            count in 10_000_000..99_000_000 -> {
                return if (countArray[2].equals('0')) "${countArray[0]}${countArray[1]}M" else "${countArray[0]}${countArray[1]}.${countArray[2]}M"
            }

            else -> return "${countArray[0]}${countArray[1]}${countArray[2]}.${countArray[3]}M"
        }
    }

}
package ru.netology.nework.adapter

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.VideoView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Post
import ru.netology.nework.util.AndroidUtil

interface onInteractionPostListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onRemove(postId: Long) {}
    fun onEdit(post: Post) {}
    fun onPlayVideo(post: Post, videoView: VideoView? = null, videoFrame: FrameLayout) {}
    fun onPlayAudio(post: Post) {}
    fun onLink(post: Post) {}
    fun openPost(post: Post) {}
    fun openPicture(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: onInteractionPostListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: onInteractionPostListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            postsAuthor.text = post.author
            postsContent.text = post.content
            postsPublished.text = AndroidUtil.formatDate(post.published)
            likesButton.text = post.setCount(post.likeOwnerIds?.size ?: 0)
//            shareButton.text = post.setCount(post.sharesCount)
//            viewsButton.text = post.setCount(post.viewsCount)
            likesButton.isChecked = post.likedByMe
            likesButton.setOnClickListener {
                likesButton.isChecked = !likesButton.isChecked
                onInteractionListener.onLike(post)
            }
            shareButton.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            menuButton.isVisible = post.ownedByMe
            menuButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post.id)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            videoFrame.isVisible = false

            link.isVisible = (post.link != null)

            if (post.link != null) {
                link.text = post.link
            }

            playVideoBtn.setOnClickListener {
                onInteractionListener.onPlayVideo(post, videoView, videoFrame)
            }
            playAudioBtn.setOnClickListener {
                onInteractionListener.onPlayAudio(post)
            }

            cardPost.setOnClickListener {
                onInteractionListener.openPost(post)
            }

            attachmentImage.setOnClickListener {
                onInteractionListener.openPicture(post)
            }

            Glide.with(postAvatar)
                .load(post.authorAvatar)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_avatar_placeholder_2)
                .circleCrop()
                .timeout(10_000)
                .into(postAvatar)

            if (post.attachment != null) {
                when (post.attachment.type) {
                    AttachmentType.IMAGE -> {
                        attachmentImage.isVisible = true
                        Glide.with(attachmentImage)
                            .load(post.attachment.url)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_error)
                            .apply(
                                RequestOptions.overrideOf(
                                    Resources.getSystem().displayMetrics.widthPixels
                                )
                            )
                            .timeout(10_000)
                            .into(attachmentImage)
                        attachmentImage.layoutParams.height
                    }

                    AttachmentType.AUDIO -> {
                        audioFrame.isVisible = true
                    }

                    AttachmentType.VIDEO -> {
                        videoFrame.isVisible = true

                    }
                }
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
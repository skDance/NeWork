package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.dto.User

interface onInteractionUserListener {
    fun onSelectUser(user: User) {}
}

class UserAdapter(
    private val onInteractionUserListener: onInteractionUserListener
) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onInteractionUserListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onInteractionListener: onInteractionUserListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) {
        binding.apply {
            userName.text = user.name
            Glide.with(userAvatar)
                .load(user.avatar)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_avatar_placeholder_2)
                .circleCrop()
                .timeout(10_000)
                .into(userAvatar)
            cardUser.setOnClickListener {
                onInteractionListener.onSelectUser(user)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
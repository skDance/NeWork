package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.util.AndroidUtil
import kotlin.random.Random
import kotlin.random.nextInt

interface onInteractionEventListener {
    fun onJoin(event: Event) {}
    fun onParticipants(event: Event) {}
    fun onSpeakers(event: Event) {}
    fun onAuthorName(event: Event) {}
    fun onAuthorJob(event: Event) {}
}

class EventAdapter(
    private val onInteractionEventListener: onInteractionEventListener
) : ListAdapter<Event, EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding, onInteractionEventListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onInteractionListener: onInteractionEventListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {
        binding.apply {
            eventDate.text = AndroidUtil.formatDate(event.published)
            eventTitle.text = event.content
            eventAuthor.text = event.author
            eventAuthorJob.text = event.authorJob
            if (event.authorJob != null) {
                eventAuthorJob.isVisible = true
                circleBarrier.isVisible = true
            }
            joinButton.isChecked = event.participatedByMe
            joinButton.setOnClickListener {
                onInteractionListener.onJoin(event)
                if (joinButton.isChecked) {
                    joinButton.setText(R.string.joinTrue)
                } else {
                    joinButton.setText(R.string.joinFalse)
                }
            }
            participantsButton.setOnClickListener {
                onInteractionListener.onParticipants(event)
            }
            speakersButton.setOnClickListener {
                onInteractionListener.onSpeakers(event)
            }

            Glide.with(eventImage)
                .load(event.attachment?.url)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(randomPicture(Random.nextInt(1..4)))
                .timeout(10_000)
                .into(eventImage)
        }
    }
}

fun randomPicture(pictureId: Int): Int {
    return when (pictureId) {
        1 -> {
            R.drawable.ic_event_placeholder_1
        }

        2 -> {
            R.drawable.ic_event_placeholder_2
        }

        3 -> {
            R.drawable.ic_event_placeholder_3
        }

        4 -> {
            R.drawable.ic_event_placeholder_4
        }

        else -> throw Exception()
    }
}


class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}
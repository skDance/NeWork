package ru.netology.nework.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.UserAdapter
import ru.netology.nework.adapter.onInteractionUserListener
import ru.netology.nework.databinding.FragmentParticipantsDialogBinding
import ru.netology.nework.dto.User
import ru.netology.nework.viewModel.EventViewModel


const val EVENT_ID = "event id"
const val TYPE = "speakers or participants"
const val DIALOG = "dialog"

@AndroidEntryPoint
class ParticipantsDialogFragment : DialogFragment() {

    companion object {
        var Bundle.eventId: Long
            set(value) = putLong(EVENT_ID, value)
            get() = getLong(EVENT_ID)

        var Bundle.type: String?
            set(value) = putString(TYPE, value)
            get() = getString(TYPE)
    }

    private val eventViewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentParticipantsDialogBinding.inflate(
            inflater,
            container,
            false
        )

        var event = eventViewModel.openEventDialogId.value

        val adapter = UserAdapter(object: onInteractionUserListener {
            override fun onSelectUser(user: User) {
                super.onSelectUser(user)
            }
        })

        val recyclerView: RecyclerView
        recyclerView = binding.list
        recyclerView.adapter = adapter


        eventViewModel.openEventDialogId.observe(viewLifecycleOwner) {
            when (arguments?.type) {
                "speakers" -> {}
                "participants" -> eventViewModel.getParticipants(event!!)
            }

        }

        eventViewModel.participants1.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }


        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            builder
                .setTitle("Список участников")
                .setView(inflater.inflate(R.layout.fragment_participants_dialog, null))
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
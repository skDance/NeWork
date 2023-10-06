package ru.netology.nework.activity

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.adapter.UserAdapter
import ru.netology.nework.adapter.onInteractionUserListener
import ru.netology.nework.databinding.FragmentParticipantsDialogBinding
import ru.netology.nework.dto.User
import ru.netology.nework.viewModel.EventViewModel
import ru.netology.nework.viewModel.UserViewModel


const val EVENT_ID = "event id"
const val TYPE = "speakers or participants or addSpeakers"
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
    private val userViewModel: UserViewModel by activityViewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = FragmentParticipantsDialogBinding.inflate(layoutInflater)
        var event = eventViewModel.openEventDialogId.value

        val adapter = UserAdapter(object : onInteractionUserListener {
            override fun onSelectUser(user: User) {
                if (arguments?.type == "addSpeakers") {
                    eventViewModel.pickSpeaker.value = user
                    findNavController().navigateUp()
                } else {
                    super.onSelectUser(user)
                }
            }
        })

        val recyclerView: RecyclerView = binding.list
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(true)
            .create()

        eventViewModel.openEventDialogId.observe(dialog) {
            when (arguments?.type) {
                "participants" -> {}
                "speakers" -> {}
                "addSpeakers" -> {
                    userViewModel.getUsers()
                }
            }
            eventViewModel.getEventUsers(event!!, arguments?.type!!)
            binding.dialogTitle.text =
                if (arguments?.type == "participants") "Участники" else "Спикеры"
        }

        eventViewModel.dialogUsers.observe(dialog) {
            adapter.submitList(it)
        }

        userViewModel.data.asLiveData().observe(dialog) {
            adapter.submitList(it.users)
        }

        return dialog
    }

    override fun onDestroy() {
        eventViewModel.cleareDialogUsers()
        super.onDestroy()
    }
}
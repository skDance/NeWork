package ru.netology.nework.activity

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.activity.ParticipantsDialogFragment.Companion.eventId
import ru.netology.nework.activity.ParticipantsDialogFragment.Companion.type
import ru.netology.nework.adapter.EventAdapter
import ru.netology.nework.adapter.onInteractionEventListener
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.util.ConstValue
import ru.netology.nework.viewModel.AuthViewModel
import ru.netology.nework.viewModel.EventViewModel
import javax.inject.Inject


@AndroidEntryPoint
class EventsFragment : Fragment() {

    private val viewModel: EventViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth

    val mediaPlayer = MediaPlayer()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentEventsBinding.inflate(
            inflater,
            container,
            false
        )



        val adapter = EventAdapter(object : onInteractionEventListener {
            override fun onJoin(event: Event) {
                if (!authViewModel.authorized) showDialog(ConstValue.SING_IN) else viewModel.joinById(event)
            }

            override fun onParticipants(event: Event) {
                if (event.participantsIds.isNotEmpty()) {
                    viewModel.openEventDialogId.value = event
                    findNavController().navigate(R.id.action_eventsFragment_to_participantsDialogFragment,
                        Bundle().apply {
                            eventId = event.id
                            type = "participants"
                        })
                } else {
                    Snackbar.make(binding.root, "List is empty", Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { viewModel.loadEvents() }
                        .show()
                }
            }

            override fun onSpeakers(event: Event) {
                if (event.speakerIds.isNotEmpty()) {
                    viewModel.openEventDialogId.value = event
                    findNavController().navigate(R.id.action_eventsFragment_to_participantsDialogFragment,
                        Bundle().apply {
                            eventId = event.id
                            type = "speakers"
                        })
                } else {
                    Snackbar.make(binding.root, "List is empty", Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { viewModel.loadEvents() }
                        .show()
                }
            }

            override fun onEdit(event: Event) {
                viewModel.edit(event)
                findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
            }

            override fun onRemove(eventId: Long) {
                viewModel.removeById(eventId)
            }

            override fun onAuthorName(event: Event) {
                super.onAuthorName(event)
            }

            override fun onAuthorJob(event: Event) {
                super.onAuthorJob(event)
            }


        })

        binding.list.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadEvents() }
                    .show()
            }
        }

        viewModel.data.asLiveData().observe(viewLifecycleOwner) { data ->
            adapter.submitList(data.events)
            binding.emptyText.isVisible = data.empty
        }

        val itemAnimator = binding.list.itemAnimator
        if (itemAnimator is DefaultItemAnimator) {
            itemAnimator.supportsChangeAnimations = false
        }

        binding.swipeRefresh.setOnRefreshListener { viewModel.refreshPosts() }

        var menuProvider: MenuProvider? = null

        authViewModel.state.observe(viewLifecycleOwner) {
            menuProvider?.let { requireActivity().removeMenuProvider(it) }

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.main_menu, menu)

                    menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                    menu.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.signout -> {
                            showDialog(ConstValue.SING_OUT)
                            true
                        }

                        R.id.signin -> {
                            findNavController().navigate(R.id.action_eventsFragment_to_authorizationFragment)
                            true
                        }

                        R.id.signup -> {
                            findNavController().navigate(R.id.action_eventsFragment_to_registrationFragment)
                            true
                        }

                        else -> false
                    }
                }
            }.apply { menuProvider = this }, viewLifecycleOwner)
        }

        binding.fab.setOnClickListener {
            if (!authViewModel.authorized) {
                showDialog(ConstValue.SING_IN)
            } else {
                findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadEvents()
            binding.swipeRefresh.isRefreshing = false
        }

        return binding.root
    }

    fun showDialog(action: String) {
        val dialog = AlertDialog.Builder(context)
        when (action) {
            ConstValue.SING_IN -> {
                dialog
                    .setTitle(R.string.dialog_auth_title)
                    .setMessage(R.string.dialog_auth_text)
                    .setPositiveButton(R.string.dialog_positive_button) { dialog, _ ->
                        findNavController().navigate(R.id.action_eventsFragment_to_authorizationFragment)
                        dialog.cancel()
                    }
                    .setNegativeButton(R.string.dialog_negative_button) { dialog, _ ->
                        dialog.cancel()
                    }
                    .create()
            }

            ConstValue.SING_OUT -> {
                dialog
                    .setTitle(R.string.dialog_logout_title)
                    .setPositiveButton(R.string.dialog_positive_button) { dialog, _ ->
                        appAuth.clear()
                        dialog.cancel()
                    }
                    .setNegativeButton(R.string.dialog_negative_button) { dialog, _ ->
                        dialog.cancel()
                    }
                    .create()
            }
        }

        dialog.show()
    }
}
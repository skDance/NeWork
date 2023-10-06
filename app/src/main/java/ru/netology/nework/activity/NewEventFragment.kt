package ru.netology.nework.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.activity.ParticipantsDialogFragment.Companion.type
import ru.netology.nework.adapter.UserAdapter
import ru.netology.nework.adapter.onInteractionUserListener
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.EventType
import ru.netology.nework.dto.User
import ru.netology.nework.util.AndroidUtil
import ru.netology.nework.viewModel.EventViewModel
import ru.netology.nework.viewModel.UserViewModel
import ru.netology.nework.viewModel.emptyEvent
import java.util.Calendar

const val USER_ID = "user id"

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val viewModel: EventViewModel by activityViewModels()

    private var attachment: Attachment? = null
    private var attachmentType: AttachmentType? = null

    private var speakersId: List<Long> = emptyList()
    private var pickUser: List<User> = emptyList()
    private var eventType: EventType = EventType.OFFLINE
    private val eventTypesArray = arrayOf("OFFLINE", "ONLINE")

    private var date = ""
    private var dateTime = ""

    private val photoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> Toast.makeText(
                    requireContext(),
                    "Photo picker error",
                    Toast.LENGTH_SHORT
                ).show()

                Activity.RESULT_OK -> {
                    val uri: Uri? = it.data?.data
                    viewModel.changeMedia(uri, uri?.toFile(), AttachmentType.IMAGE)
                }
            }
        }

    companion object {
        var Bundle.user: Long
            set(value) = putLong(EVENT_ID, value)
            get() = getLong(EVENT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewEventBinding.inflate(layoutInflater)

        var adapter = UserAdapter(object : onInteractionUserListener {
            override fun onSelectUser(user: User) {
                pickUser = pickUser.filter { it != user }
                viewModel.pickSpeaker.value = null
            }
        })

        viewModel.editedEvent.observe(viewLifecycleOwner) {
            with(binding) {
                if (it != emptyEvent) {
                    eventTitle.setText(it.content)
                    val editDate = AndroidUtil.formatDate(it.datetime)
                    val dateArray = editDate.split(" ")
                    eventTime.setText(dateArray[1])
                    dateTime = dateArray[1]
                    eventDate.setText(dateArray[0])
                    date = dateArray[1]
                    eventLink.setText(it.link)
                    viewModel.getEventUsers(it, "edit event")

                    Glide.with(attachImage)
                        .load(it.attachment?.url)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .timeout(10_000)
                        .into(attachImage)
                }
            }
        }

        viewModel.editedSpeakers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        val speakersId = pickUser.map { it.id }
                        val date = "${binding.eventDate.text}T${binding.eventTime.text}:00.002Z"
                        viewModel.changeContent(
                            content = binding.eventTitle.text.toString(),
                            datetime = date,
                            link = binding.eventLink.text.toString(),
                            type = eventType,
                            speakerIds = speakersId,
                        )
                        viewModel.save()
                        AndroidUtil.hideKeyboard(requireView())
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)

        viewModel.media.observe(viewLifecycleOwner) { media ->
            binding.attachImage.isVisible = true
            binding.attachImage.setImageURI(media.uri)
            binding.removeImage.isVisible = true
        }

        binding.removeImage.setOnClickListener {
            viewModel.deleteAttachment()
            attachment = null
            viewModel.changeMedia(null, null, null)
            binding.attachImage.isVisible = false
            binding.removeImage.isVisible = false
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            viewModel.loadEvents()
            findNavController().navigateUp()
        }

        binding.list.adapter = adapter

        binding.addSpeaker.setOnClickListener {
            findNavController().navigate(R.id.action_newEventFragment_to_participantsDialogFragment,
                Bundle().apply {
                    type = "addSpeakers"
                })
        }

        val eventTime = binding.eventTime
        eventTime.showSoftInputOnFocus = false
        eventTime.setOnClickListener {
            val dialog = TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                    dateTime = "$hour:$minute"
                    eventTime.setText(dateTime)
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true,
            )
            dialog.show()
        }

        val eventDate = binding.eventDate
        eventDate.showSoftInputOnFocus = false
        eventDate.setOnClickListener {
            val dialog = DatePickerDialog(
                requireContext(),
                null,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            dialog.setOnDateSetListener { datePicker, year, month, day ->
                date = "$year-${month + 1}-$day"
                eventDate.setText(date)
            }
            dialog.show()
        }

        val eventTypeSpinner = binding.spinner
        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            eventTypesArray
        )
        eventTypeSpinner.adapter = arrayAdapter
        eventTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (eventTypesArray[p2] == "OFFLINE") {
                    eventType = EventType.OFFLINE
                } else {
                    eventType = EventType.ONLINE
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        viewModel.pickSpeaker.observe(viewLifecycleOwner) {
            if (it != null) {
                speakersId += it.id
                pickUser += it
                binding.list.isVisible = pickUser.isNotEmpty()
                adapter.submitList(pickUser)
            } else {
                adapter.submitList(pickUser)
            }
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
            binding.attachImage.isVisible = true
        }



        return binding.root
    }
}


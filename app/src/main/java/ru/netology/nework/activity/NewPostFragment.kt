package ru.netology.nework.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.util.AndroidUtil
import ru.netology.nework.util.StringArg
import ru.netology.nework.viewModel.PostViewModel
import java.io.File
import java.io.FileOutputStream


@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private var attachment: Attachment? = null
    private var attachmentType: AttachmentType? = null

    companion object {
        var Bundle.edit: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()

    private val photoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> Toast.makeText(
                    requireContext(),
                    "Photo picker error",
                    Toast.LENGTH_SHORT
                ).show()

                Activity.RESULT_OK -> {
                    val uri = it.data?.data ?: return@registerForActivityResult
                    val file = uri.toFile()
                    viewModel.changeMedia(uri, uri?.toFile(), AttachmentType.IMAGE)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.edit?.let(binding.edit::setText)
        binding.edit.setText(arguments?.getString("editPostText"))

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        viewModel.changeContent(binding.edit.text.toString())
                        viewModel.save()
                        AndroidUtil.hideKeyboard(requireView())
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner)

        viewModel.media.observe(viewLifecycleOwner) { media ->
            if (media.uri == null && attachment?.url.isNullOrBlank()) {
                binding.photoContainer.isVisible = false
                return@observe
            } else {
                binding.photoContainer.visibility = View.VISIBLE
                if (media.attachmentType == AttachmentType.IMAGE) {
                    binding.preview.setImageURI(media.uri)
                }
            }
            binding.photoContainer.isVisible = true
            binding.preview.setImageURI(media.uri)
        }

        binding.removeAttach.setOnClickListener {
            viewModel.deleteAttachment()
            attachment = null
            viewModel.changeMedia(null, null, null)
            binding.preview.isVisible = false
            binding.videoPlaceholder.isVisible = false
            binding.audioPlaceholder.isVisible = false
        }

        binding.edit.requestFocus()
        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }
        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
            binding.preview.isVisible = true
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
            binding.preview.isVisible = true
        }

        val pickMediaLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        val contentResolver = context?.contentResolver
                        val inputStream = uri?.let { it1 -> contentResolver?.openInputStream(it1) }
                        val audioBytes = inputStream?.readBytes()

                        if (uri != null && contentResolver != null) {
                            val extension = MimeTypeMap.getSingleton()
                                .getExtensionFromMimeType(contentResolver.getType(uri) ?: null)
                            val file = File(context?.getExternalFilesDir(null), "input.$extension")
                            FileOutputStream(file).use { outputStream ->
                                outputStream.write(audioBytes)
                                outputStream.flush()
                            }
                            viewModel.changeMedia(uri, file, attachmentType)
                        }
                    }

                    else -> {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.error_upload),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }

        binding.addAudio.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            intent.type = "audio/*"
            attachmentType = AttachmentType.AUDIO
            attachment = attachment?.copy(type = attachmentType!!)
            try {
                pickMediaLauncher.launch(intent)
                binding.audioPlaceholder.isVisible = true
            } catch (e: Exception) {
                println(e)
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_upload),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        binding.addVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            attachmentType = AttachmentType.VIDEO
            attachment = attachment?.copy(type = attachmentType!!)
            pickMediaLauncher.launch(intent)
            binding.videoPlaceholder.isVisible = true
        }

        return binding.root
    }
}
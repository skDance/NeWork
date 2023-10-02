package ru.netology.nework.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegistrationBinding
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.util.AndroidUtil
import ru.netology.nework.viewModel.RegistrationViewModel
import kotlin.coroutines.EmptyCoroutineContext


@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private val regViewModel: RegistrationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentRegistrationBinding.inflate(
            inflater,
            container,
            false
        )

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        regViewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

        binding.regAvatar.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(512)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        regViewModel.avatar.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.regRemoveAvatarBtn.visibility = View.GONE
                binding.regAvatar.setImageResource(R.drawable.ic_reg_avatar_placeholder)
            } else {
                binding.regRemoveAvatarBtn.visibility = View.VISIBLE
                binding.regAvatar.setImageURI(it.uri)
            }
        }

        with(binding) {
            regRemoveAvatarBtn.setOnClickListener {
                regViewModel.changePhoto(null, null)
            }
            regButton.setOnClickListener {
                when {
                    regLogin.text.isNullOrBlank() || regPassword.text.isNullOrBlank() -> Toast.makeText(
                        activity,
                        R.string.error_signin_log_or_pas,
                        Toast.LENGTH_LONG
                    ).show()

                    regPasswordConfirm.text.isNullOrBlank() -> Toast.makeText(
                        activity,
                        R.string.reg_error_password_confirm_null,
                        Toast.LENGTH_LONG
                    ).show()

                    regPassword.text.toString() != regPasswordConfirm.text.toString() -> Toast.makeText(
                        activity,
                        R.string.reg_error_password_confirm,
                        Toast.LENGTH_LONG
                    ).show()

                    else -> {
                        val login = regLogin.text.toString()
                        val password = regPassword.text.toString()
                        val name = regName.text.toString()
                        val avatar =
                            regViewModel.avatar.value?.file?.let { file -> MediaUpload(file) }
                        CoroutineScope(EmptyCoroutineContext).launch {
                            regViewModel.registration(
                                login, password, name, avatar
                            )
                        }
                        AndroidUtil.hideKeyboard(requireView())
                    }
                }
            }
        }

        regViewModel.registered.observe(viewLifecycleOwner) { state ->
            binding.regProgress.isVisible = state.authLoading
            if (state.authSuccessful) {
                findNavController().navigateUp()
            } else if (state.authError) {
                Toast.makeText(activity, R.string.error_signin, Toast.LENGTH_LONG).show()
                binding.regPassword.text.clear()
                binding.regPasswordConfirm.text.clear()
            }
        }

        return binding.root
    }
}
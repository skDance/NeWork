package ru.netology.nework.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentAuthorizationBinding
import ru.netology.nework.util.AndroidUtil
import ru.netology.nework.viewModel.LoginViewModel
import kotlin.coroutines.EmptyCoroutineContext


class AuthorizationFragment : Fragment() {

    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAuthorizationBinding.inflate(
            inflater,
            container,
            false
        )

        with(binding) {
            authButton.setOnClickListener {
                if (login.text.isNullOrBlank() || password.text.isNullOrBlank()) {
                    Toast.makeText(activity, R.string.error_signin_log_or_pas, Toast.LENGTH_LONG).show()
                } else {
                    CoroutineScope(EmptyCoroutineContext).launch {
                        loginViewModel.authorization(binding.login.text.toString(), binding.password.text.toString())
                    }
                    AndroidUtil.hideKeyboard(requireView())
                }
            }
        }

        loginViewModel.authorized.observe(viewLifecycleOwner) {state ->
            binding.authProgress.isVisible = state.authLoading
            if (state.authSuccessful) {
                findNavController().navigateUp()
            } else if (state.authError) {
                Toast.makeText(activity, R.string.error_signin, Toast.LENGTH_LONG).show()
                binding.password.text.clear()
            }
        }

        return binding.root
    }
}
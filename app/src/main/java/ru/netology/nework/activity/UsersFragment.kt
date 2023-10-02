package ru.netology.nework.activity

import android.app.AlertDialog
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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.UserAdapter
import ru.netology.nework.adapter.onInteractionUserListener
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.dto.User
import ru.netology.nework.util.ConstValue
import ru.netology.nework.viewModel.AuthViewModel
import ru.netology.nework.viewModel.UserViewModel
import javax.inject.Inject


@AndroidEntryPoint
class UsersFragment : Fragment() {

    private val viewModel: UserViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentUsersBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = UserAdapter(object: onInteractionUserListener {
            override fun onSelectUser(user: User) {
                super.onSelectUser(user)
            }
        })

        binding.list.adapter = adapter
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.getUsers() }
                    .show()
            }
        }

        viewModel.data.asLiveData().observe(viewLifecycleOwner) { data ->
            adapter.submitList(data.users)
            binding.emptyText.isVisible = data.empty
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshUsers()
            binding.swipeRefresh.isRefreshing = false
        }

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
                            findNavController().navigate(R.id.action_postsFragment_to_authorizationFragment)
                            true
                        }

                        R.id.signup -> {
                            findNavController().navigate(R.id.action_postsFragment_to_registrationFragment)
                            true
                        }

                        else -> false
                    }
                }
            }.apply { menuProvider = this }, viewLifecycleOwner)
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
                        findNavController().navigate(R.id.action_postsFragment_to_authorizationFragment)
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
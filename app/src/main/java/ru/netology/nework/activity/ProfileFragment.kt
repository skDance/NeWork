package ru.netology.nework.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapter.JobAdapter
import ru.netology.nework.adapter.onInteractionJobListener
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentProfileBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.User
import ru.netology.nework.viewModel.AuthViewModel
import ru.netology.nework.viewModel.JobViewModel
import ru.netology.nework.viewModel.UserViewModel
import ru.netology.nework.viewModel.emptyUser
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private val usersViewModel: UserViewModel by viewModels()
    private val jobViewModel: JobViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth
    lateinit var user: User

    companion object {
        var Bundle.userId: Long
            set(value) = putLong(EVENT_ID, value)
            get() = getLong(EVENT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentProfileBinding.inflate(layoutInflater)

        lifecycleScope.launchWhenCreated {
            usersViewModel.data.collectLatest { listUsers ->
                user = listUsers.users?.find {
                    it.id == (arguments?.userId ?: appAuth.authStateFlow.value.id)
                } ?: emptyUser

                Glide.with(binding.userAvatar)
                    .load(user.avatar)
                    .placeholder(R.drawable.ic_avatar_placeholder_2)
                    .error(R.drawable.ic_avatar_placeholder_2)
                    .circleCrop()
                    .timeout(10_000)
                    .into(binding.userAvatar)
                binding.userName.text = user.name
                jobViewModel.loadMyJobs()
            }
        }

        if (arguments?.userId != null) {
            jobViewModel.loadUserJobs(arguments?.userId!!)
        } else {
            jobViewModel.loadMyJobs()
        }

        val jobAdapter = JobAdapter(object : onInteractionJobListener {
            override fun onEdit(job: Job) {
                jobViewModel.edit(job)
                findNavController().navigate(R.id.action_profileFragment_to_newJobFragment)
            }

            override fun onRemove(jobId: Long) {
                jobViewModel.removeById(jobId)
            }
        })
        binding.list.adapter = jobAdapter

        jobViewModel.data.asLiveData().observe(viewLifecycleOwner) {
            jobViewModel.loadMyJobs()
        }

        jobViewModel.jobCreated.observe(viewLifecycleOwner) {
            jobViewModel.loadMyJobs()
        }

        jobViewModel.jobsList.observe(viewLifecycleOwner) {
            binding.list.isVisible = true
            jobAdapter.submitList(it)
        }



        binding.addJob.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_newJobFragment)
        }

        jobViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .show()
            }
        }



        return binding.root
    }

}
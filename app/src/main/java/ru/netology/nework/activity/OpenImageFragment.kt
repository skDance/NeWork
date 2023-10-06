package ru.netology.nework.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.databinding.FragmentOpenImageBinding
import ru.netology.nework.viewModel.PostViewModel


@AndroidEntryPoint
class OpenImageFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentOpenImageBinding.inflate(
            inflater,
            container,
            false
        )

        viewModel.openPicture.observe(viewLifecycleOwner) {
            Glide.with(binding.picture)
                .load(it)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_error)
                .timeout(10_000)
                .into(binding.picture)
        }
        return binding.root
    }
}
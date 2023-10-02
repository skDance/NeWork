package ru.netology.nework.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewEventBinding

class NewEventFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }
}
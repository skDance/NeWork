package ru.netology.nework.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewJobBinding
import ru.netology.nework.util.AndroidUtil
import ru.netology.nework.util.AndroidUtil.hideKeyboard
import ru.netology.nework.viewModel.JobViewModel
import ru.netology.nework.viewModel.emptyEvent
import ru.netology.nework.viewModel.emptyJob
import java.util.Calendar

@AndroidEntryPoint
class NewJobFragment : Fragment() {

    private val viewModel: JobViewModel by activityViewModels()

    private var dateStart = ""
    private var dateFinish: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewJobBinding.inflate(layoutInflater)

        viewModel.editedJob.observe(viewLifecycleOwner) { job ->
            with(binding) {
                if (job != emptyJob) {
                    name.setText(job?.name)
                    position.setText(job?.position)
                    val editStart = AndroidUtil.formatDate(job!!.start).split(" ").get(0)
                    val editFinish =
                        job.finish?.let { it1 -> AndroidUtil.formatDate(it1) }?.split(" ")?.get(0)
                    periodStart.setText(editStart)
                    periodFinish.setText(editFinish)
                    link.setText(job.link)
                }
            }

        }

        val jobStart = binding.periodStart
        val jobFinish = binding.periodFinish

        jobStart.showSoftInputOnFocus = false
        jobFinish.showSoftInputOnFocus = false

        jobStart.setOnClickListener {
            val dialog = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                    dateStart = "$year-${month + 1}-${day}T01:01:01.001Z"
                    jobStart.setText("$year-${month + 1}-$day")
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        jobFinish.setOnClickListener {
            val dialog = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                    dateFinish = "$year-${month + 1}-${day}T01:01:01.001Z"
                    jobFinish.setText("$year-${month + 1}-$day")
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.addJobBtn.setOnClickListener {
            viewModel.changeContent(
                binding.name.text.toString(),
                binding.position.text.toString(),
                dateStart,
                dateFinish,
                binding.link.text.toString()
            )
            viewModel.save()
            hideKeyboard(requireView())
            findNavController().navigateUp()
        }

        return binding.root
    }

}
package com.example.smarcifier.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smarcifier.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment() {

    private var _binding: FragmentAlarmBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var highTempInput: EditText
    lateinit var lowTempInput: EditText

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val alarmViewModel =
                ViewModelProvider(this).get(AlarmViewModel::class.java)

        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val buttonSetValue = binding.buttonSetValue
        buttonSetValue.setOnClickListener {
            highTempInput = binding.editTextHighTemp
            var highValue = highTempInput.text

            lowTempInput = binding.editTextLowTemp
            var lowValue = lowTempInput.text

            Toast.makeText(activity, "$highValue, $lowValue", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
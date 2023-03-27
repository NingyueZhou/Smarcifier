package com.example.smarcifier.ui.temp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smarcifier.databinding.FragmentTempBinding

class TempFragment : Fragment() {

    private var _binding: FragmentTempBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val tempViewModel =
                ViewModelProvider(this).get(TempViewModel::class.java)
        com.example.smarcifier.R.layout.fragment_temp
        _binding = FragmentTempBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textTemperature: TextView = binding.textTemperature
        tempViewModel.text.observe(viewLifecycleOwner) {
            textTemperature.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
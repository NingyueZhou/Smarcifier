package com.example.smarcifier.ui.temp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smarcifier.databinding.FragmentTempBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

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

        //setLineChart
        val lineChart = binding.lineChart

        //Part1
        val entries = ArrayList<Entry>()

        //Part2
        entries.add(Entry(1f, 36.5f))
        entries.add(Entry(2f, 36.0f))
        entries.add(Entry(3f, 38.5f))
        entries.add(Entry(4f, 35.0f))
        entries.add(Entry(5f, 36.5f))

        //Part3
        //val vl = LineDataSet(entries, "My Type")
        val lineDataSet = LineDataSet(entries, "")
        lineChart.getLegend().setEnabled(false)
        //lineChart.getXAxis().setDrawLabels(false)

        //Part4
        lineDataSet.setDrawValues(true)
        lineDataSet.setDrawFilled(true)
        lineDataSet.lineWidth = 3f
        //vl.fillColor = R.color.gray
        //vl.fillAlpha = R.color.red

        //Part5
        lineChart.xAxis.labelRotationAngle = 0f

        //Part6
        lineChart.data = LineData(lineDataSet)

        //Part7
        lineChart.axisRight.isEnabled = false
        //binding.lineChart.xAxis.axisMaximum = j+0.1f

        //Part8
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        //Part9
        //lineChart.description.text = "Days"
        lineChart.description.text = ""
        lineChart.setNoDataText("No forex yet!")

        //Part10
        lineChart.animateX(1800, Easing.EaseInExpo)

        //Part11
        //val markerView = CustomMarker(activity, R.layout.fragment_settings)
        //lineChart.marker = markerView

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//class CustomMarker(context: FragmentActivity?, layoutResource: Int) : MarkerView(context, layoutResource) {}
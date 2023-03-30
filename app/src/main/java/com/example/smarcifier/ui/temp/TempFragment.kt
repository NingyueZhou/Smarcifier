package com.example.smarcifier.ui.temp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smarcifier.MainActivity
import com.example.smarcifier.R
import com.example.smarcifier.databinding.FragmentTempBinding
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

class TemperatureValueFormatter : ValueFormatter()
{
    override fun getPointLabel(entry: Entry?): String {
        if (entry == null) return "";
        return "%.1f".format(entry.y);
    }
}

class TimeFormatter : ValueFormatter()
{
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val millis = value.toLong();
        return "%02d:%02d:%02d".format(
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        );
    }
}

class TempFragment : Fragment()
{
    private var _binding: FragmentTempBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val MAX_TEMP_ENTRIES = 20;
    private val entries: LineDataSet = LineDataSet(ArrayList<Entry>(), "Temperature");

    init {
        //entries.setDrawValues(false)
        entries.valueFormatter = TemperatureValueFormatter()
        entries.setDrawFilled(true)
        // entries.mode = LineDataSet.Mode.CUBIC_BEZIER
        entries.lineWidth = 3f

        for (i in 0..MAX_TEMP_ENTRIES) {
            entries.addEntry(Entry(i.toFloat(), 35.0f));
        }
    }

    private fun onNewTemperature(newTemp: Float) {
        if (_binding == null) return;

        // Update text view
        val textView = binding.root.findViewById<TextView>(R.id.textTemperature);
        textView?.post(Runnable() {
            textView.text = "%.1f".format(newTemp);
        })

        // Add item to graph
        val curTime = System.currentTimeMillis() % (1000 * 60 * 60 * 24);
        val rounded = newTemp.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toFloat();
        entries.addEntry(Entry(curTime.toFloat(), rounded));
        updateGraph();
    }

    private fun updateGraph() {
        while (entries.entryCount > MAX_TEMP_ENTRIES) {
            entries.removeFirst()
        }

        val lineChart = binding.lineChart
        lineChart.data = LineData(entries)
        lineChart.invalidate();
    }

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

        (activity as MainActivity?)?.setOnTemperatureChangeCallback(::onNewTemperature);

        // Configure the line chart
        val lineChart = binding.lineChart
        lineChart.legend.isEnabled = false
        lineChart.xAxis.setDrawLabels(true)
        lineChart.xAxis.labelRotationAngle = 0f
        lineChart.xAxis.valueFormatter = TimeFormatter()
        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.isEnabled = false;
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.description.text = ""
        lineChart.setNoDataText("Connect to your Bo-Bo to see a measurement")

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//class CustomMarker(context: FragmentActivity?, layoutResource: Int) : MarkerView(context, layoutResource) {}

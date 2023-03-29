package com.example.smarcifier.ui.settings

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothtest.BoboConnection
import com.example.smarcifier.BluetoothInstance
import com.example.smarcifier.DeviceListAdapter
import com.example.smarcifier.MainActivity
import com.example.smarcifier.R
import com.example.smarcifier.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment()
{
    private var context: Context? = null;
    private var _binding: FragmentSettingsBinding? = null

    private var bluetooth: BluetoothInstance? = null;

    // The list of all discovered Bluetooth devices.
    private val btDeviceList: DeviceListAdapter = DeviceListAdapter(::onDeviceClick);

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        context = inflater.context;
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Initialize bluetooth stack
        bluetooth = BluetoothInstance(inflater.context, this, ::onInitialize);

        // Set up device list
        val view = binding.root
        val listElem = view.findViewById<RecyclerView>(R.id.bt_device_list)
        listElem.adapter = btDeviceList

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onInitialize(bt: BluetoothInstance?) {
        if (bt != null) {
            // The instance is valid - start scanning for devices
            bt.scanForBleDevices(object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    if (result != null) {
                        btDeviceList.addDevice(result.device);
                    }
                }
            })
        }
        else {
            // Try again?
            // bluetooth = BluetoothInstance(this, ::onInitialize);
        }
    }

    private fun onDeviceClick(device: BluetoothDevice) {
        val newCon = BoboConnection(context!!, device) {}

        // HACK: Access the activity directly because I don't know how to access
        // the fragment in MainActivity::onCreate. The clean way to do this
        // would be a callback interface that the MainActivity can instantiate
        // and pass to the SettingsFragment.
        (activity as MainActivity?)?.notifyBluetoothConnect(newCon);
    }
}
package com.example.bluetoothtest

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

/**
 * A list of devices. Creates a button for each device.
 *
 * @param onDeviceClick A callback that is called whenever the user clicks on a
 *                      button in the list.
 */
class DeviceListAdapter(private val onDeviceClick: (BluetoothDevice) -> Unit)
    : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>()
{
    /**
     * Wrapper around a `View` that contains the layout for an individual item
     * in the list.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val connectButton: Button;

        init {
            connectButton = view.findViewById<Button>(R.id.device_list_elem);
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.text_row_item, parent, false);
        return ViewHolder(view);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position];
        holder.connectButton.text = device.name;
        holder.connectButton.setOnClickListener { _ ->
            onDeviceClick(device);
        };
    }

    override fun getItemCount(): Int {
        return devices.size;
    }

    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device) && device.name != null) {
            devices.add(device);
            // Log.d(LOG_TAG_BT, "Device added to the device list: ${device.name}")
            super.notifyItemInserted(devices.size - 1);
        }
    }

    fun clear() {
        super.notifyItemRangeRemoved(0, devices.size);
        devices.clear();
    }

    private var devices = mutableListOf<BluetoothDevice>();
}
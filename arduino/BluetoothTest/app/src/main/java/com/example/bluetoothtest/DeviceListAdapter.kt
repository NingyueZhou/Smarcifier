package com.example.bluetoothtest

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceListAdapter : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>()
{
    /**
     * Wrapper around a `View` that contains the layout for an individual item
     * in the list.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val textView: TextView;

        init {
            textView = view.findViewById(R.id.textView);
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.text_row_item, parent, false);
        return ViewHolder(view);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = devices[position].name;
    }

    override fun getItemCount(): Int {
        return devices.size;
    }

    public fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device);
        }
    }

    public fun clear() {
        devices.clear();
    }

    private var devices = mutableListOf<BluetoothDevice>();
}
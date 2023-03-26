package com.example.bluetoothtest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class BluetoothLeScanCallback : ScanCallback()
{
}

class MainActivity : AppCompatActivity() {
    private var bleScanner: BluetoothLeScanner? = null;
    private var scanning: Boolean = false;

    private var deviceList = DeviceListAdapter();

    /**
     * Ad-hoc implementation of a BLE scan callback
     */
    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            deviceList.addDevice(result.device);
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_connect)

        val button = findViewById<Button>(R.id.my_button);
        button.text = "Find bluetooth device";
        button.setOnClickListener { _: View ->
            setupBluetooth();
            scanForBleDevices();
        }
    }

    private fun scanForBleDevices() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH_SCAN);
            return;
        }
        //if (bleScanner == null) {
        //    setupBluetooth();
        //    return;
        //}

        assert(bleScanner != null);
        if (!scanning) {
            bleScanner?.startScan(scanCallback);
        }
    }

    /**
     * Ensures that the app has bluetooth permissions and that bluetooth is
     * enabled on the device.
     */
    private fun setupBluetooth() {
        // Explicitly request permission to use bluetooth
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH_CONNECT);
            return;
        }

        val statusText = findViewById<TextView>(R.id.status_text);

        val btManager = getSystemService(BluetoothManager::class.java);
        val btAdapter = btManager.adapter;
        if (btAdapter == null) {
            statusText.text = "Your device does not have the required bluetooth capabilities :(";
            return;
        }

        // Request that the user enable the bluetooth adapter if it is disabled.
        // We need bluetooth permission for this.
        if (!btAdapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetooth.launch(intent);
        }

        bleScanner = btAdapter.bluetoothLeScanner;
        return;

        // Explicitly request permission to scan for devices.
        // Query the scanner object if permission is granted.
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH_SCAN);
            return;
        }
        else {
            bleScanner = btAdapter.bluetoothLeScanner;
        }
    }

    /**
     * An activity that requests the BLUETOOTH_CONNECT permission from the user.
     */
    private val requestBluetoothPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val statusText = findViewById<TextView>(R.id.status_text);
        if (!isGranted) {
            statusText.text = "Bluetooth permission denied.";
        }
        else {
            statusText.text = "Bluetooth permission granted.";
        }
    };

    /**
     * An activity that requests the user to turn on bluetooth
     */
    private val enableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}
}
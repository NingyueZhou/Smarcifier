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
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

val LOG_TAG_BT = "Bluetooth";

class MainActivity : AppCompatActivity() {
    private val SCAN_TIMEOUT: Long = 30000; // 30 seconds

    private var bleScanner: BluetoothLeScanner? = null;
    private var scanning: Boolean = false;
    private val handler = Handler();

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

        // Set up button
        val button = findViewById<Button>(R.id.my_button);
        button.text = "Find bluetooth devices";
        button.setOnClickListener { _: View ->
            setupBluetooth();
            scanForBleDevices();
        }

        // Set up device list
        val list = findViewById<RecyclerView>(R.id.bt_device_list);
        list.adapter = deviceList;
        list.layoutManager = LinearLayoutManager(this@MainActivity);
    }

    private fun scanForBleDevices() {
        assert(bleScanner != null);

        // Check for scan permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH_SCAN);
            return;
        }

        // Start scanning
        if (!scanning) {
            bleScanner?.startScan(scanCallback);
            scanning = true;

            // Stop the scan after a timeout
            handler.postDelayed({
                bleScanner?.stopScan(scanCallback);
                scanning = false;
                Log.d(LOG_TAG_BT, "Scanning stopped after timeout of ${SCAN_TIMEOUT}s");
            }, SCAN_TIMEOUT);
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

        // The scanner can be retrieved even without BLUETOOTH_SCAN permissions.
        // I test for the permission when I actually use the scanner, i.e. in
        // `scanForBleDevices`.
        bleScanner = btAdapter.bluetoothLeScanner;
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
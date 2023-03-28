package com.example.bluetoothtest

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

const val LOG_TAG_BT = "Bluetooth";

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    private val SCAN_TIMEOUT: Long = 30000; // 30 seconds

    private var bleScanner: BluetoothLeScanner? = null;
    private var scanning: Boolean = false;
    private val handler = Handler();

    private val deviceList: DeviceListAdapter;
    private var boboConnection: BoboConnection? = null;

    init {
        deviceList = DeviceListAdapter { device: BluetoothDevice ->
            Log.d(LOG_TAG_BT, "Device connected: ${device.name}");
            boboConnection = BoboConnection(this, device) { newTemp ->
                val text = findViewById<TextView>(R.id.temperature_display);
                text.post(Runnable { text.text = newTemp.toString() })
            }
        };
    }

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
    }

    private fun scanForBleDevices() {
        if (bleScanner == null) return;

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
            }, SCAN_TIMEOUT);
        }
        // If scanning is in process, reset the device list and restart the scan
        else {
            bleScanner?.stopScan(scanCallback);
            scanning = false;
            deviceList.clear();
            scanForBleDevices();
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
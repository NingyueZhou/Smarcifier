package com.example.smarcifier

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Sets up the entire bluetooth stack automatically.
 *
 * @param onInitialize A callback that is called when the initialization is
 *                     completed. Takes the `BluetoothInstance` itself if
 *                     initialization was successful, or null otherwise. If
 *                     unsuccessful, the `BluetoothInstance` object becomes
 *                     useless and can be destroyed.
 */
class BluetoothInstance(
    private val context: Context,
    private val fragment: Fragment,
    onInitialize: (BluetoothInstance?) -> Unit,
) {
    private val LOG_TAG = "boboBtLog";
    private val SCAN_TIMEOUT: Long = 30000; // 30 seconds

    private var manager: BluetoothManager? = null;
    private var adapter: BluetoothAdapter? = null;
    private var bleScanner: BluetoothLeScanner? = null;
    private var initFailed = false;  // Can fail because of permissions or missing resources

    private var scanning: Boolean = false;
    private val handler = Handler();

    private val requestPermission: ActivityResultLauncher<String>;
    private val startActivity: ActivityResultLauncher<Intent>;

    init {
        requestPermission = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                initFailed = true;
            }
            setupBluetooth(onInitialize);
        };

        startActivity = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
                initFailed = true;
            }
            setupBluetooth(onInitialize);
        };

        setupBluetooth(onInitialize);
    }

    /**
     * @return Boolean Returns false if the Bluetooth instance is not
     *                 initialized. This may occur because the user has denied
     *                 the required Bluetooth permissions.
     */
    @SuppressLint("MissingPermission")
    fun scanForBleDevices(scanCallback: ScanCallback): Boolean {
        if (bleScanner == null) {
            return false;
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
            scanForBleDevices(scanCallback);
        }

        return true;
    }

    /**
     * Check if the app has been granted a permission.
     */
    private fun hasPermission(permission: String): Boolean {
        val status = ContextCompat.checkSelfPermission(context, permission)
        return status == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Set up the bluetooth stack.
     *
     * This function is called every time a permission request succeeds from the
     * respective activity launcher. If called often enough, and if every
     * permission request succeeds, it will eventually run to completion and
     * call the `onInitialize` callback with a non-null value.
     *
     * -- RANT START --
     * This simulates a coroutine, which is absolutely ridiculous since Kotlin
     * natively supports coroutines, but the Android SDK doesn't seem to care
     * about good coding practices.
     * Late initialization in 2023 kekw
     * -- RANT END ---
     */
    private fun setupBluetooth(onInitialize: (BluetoothInstance?) -> Unit) {
        if (initFailed) {
            onInitialize(null);
            return;
        }

        // Explicitly request permission to use bluetooth
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            requestPermission.launch(Manifest.permission.BLUETOOTH_CONNECT);  // Re-launches setupBluetooth
            return;
        }
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            requestPermission.launch(Manifest.permission.BLUETOOTH_SCAN);  // Re-launches setupBluetooth
            return;
        }

        // Try to query the bluetooth manager
        manager = context.getSystemService(BluetoothManager::class.java);
        if (manager == null)
        {
            initFailed = true;
            onInitialize(null);
            return;
        }

        // Try to query the bluetooth adapter. Always fail if null, but try to
        // enable it if it is disabled.
        adapter = manager?.adapter;
        if (adapter == null)
        {
            Log.d(LOG_TAG, "Your device does not have the required bluetooth capabilities :(");
            initFailed = true;
            onInitialize(null);
            return;
        }
        else {
            // Request that the user enable the bluetooth adapter if it is disabled.
            //
            // Don't get confused by the question mark; We have already asserted that
            // the adapter is not null. This exists just because Kotlin's type system
            // has not been though through all the way.
            if (adapter?.isEnabled != true) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity.launch(intent);  // Re-launches setupBluetooth
                return;
            }
        }

        // The scanner can be retrieved even without BLUETOOTH_SCAN permissions.
        // I test for the permission when I actually use the scanner, i.e. in
        // `scanForBleDevices`.
        bleScanner = adapter?.bluetoothLeScanner;

        // Finally, call onInitialize(true) iff the scanner instance is not null
        if (bleScanner != null) {
            onInitialize(this);
        }
        else {
            onInitialize(null);
        }
    }
}
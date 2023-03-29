package com.example.bluetoothtest

import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

class BoboConnection(
    context: Context,
    private val device: BluetoothDevice,
    private val onTemperatureChange: (Float) -> Unit
) {
    private val SERVICE_UUID = UUID.fromString("bbbaa765-0507-423a-9494-9cfd4d7e86fb")
    private val TEMP_UUID = UUID.fromString("673eb6f3-1af4-48db-83ac-dd9d3b0c5950")

    private var gatt: BluetoothGatt? = null;
    private var service: BluetoothGattService? = null
    private var tempChara: BluetoothGattCharacteristic? = null;

    private var currentTemperature: Float? = null

    init {
        device.connectGatt(context, false, BleCallback())
    }

    inner class BleCallback : BluetoothGattCallback()
    {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    assert(gatt != null)
                    gatt?.discoverServices()
                    this@BoboConnection.gatt = gatt
                }
                BluetoothProfile.STATE_DISCONNECTED -> {}
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            assert(gatt != null)
            assert(gatt == this@BoboConnection.gatt)

            val service = gatt?.getService(SERVICE_UUID)
            val temp = service?.getCharacteristic(TEMP_UUID)
            if (temp != null) {
                tempChara = temp
                gatt?.setCharacteristicNotification(temp, true);
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == TEMP_UUID) {
                assert(value.size == 4)
                val int = ((value[3].toUInt() and 0xffu) shl 24).or(
                    ((value[2].toUInt() and 0xffu) shl 16)).or(
                    ((value[1].toUInt() and 0xffu) shl 8)).or(
                    ((value[0].toUInt() and 0xffu) shl 0)
                )

                val newTemp = int.toFloat() * 0.001f
                currentTemperature = newTemp
                onTemperatureChange(newTemp)
            }
        }
    }

    public fun isValid(): Boolean {
        return gatt != null && service != null && tempChara != null;
    }

    public fun disconnect() {
        gatt?.disconnect()
    }

    public fun getTemperature(): Float? {
        return currentTemperature
    }
}
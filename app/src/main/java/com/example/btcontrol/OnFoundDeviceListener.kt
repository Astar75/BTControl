package com.example.btcontrol

import android.bluetooth.BluetoothDevice

interface OnFoundDeviceListener {
    fun onFoundDevice(device: BluetoothDevice)
}
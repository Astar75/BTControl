package com.example.btcontrol

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.reflect.Method
import java.net.Socket
import java.util.*

class BTService : Service() {

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var foundDeviceListener: OnFoundDeviceListener? = null
    private var progressBarStateListener: OnProgressBarStateListener? = null

    private var connectThread: ConnectThread? = null    // класс для соединения с устройством
    private var connectedThread: ConnectedThread? = null  // класс для работы с текущим соединением

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Сервис запущен... ")

        /* события, которые будет ловить broadcastReceiver */
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)

        registerReceiver(broadcastReceiver, intentFilter)

    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: Сервис работает...")
        return LocalBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: сервис уничтожен!")
    }

    inner class LocalBinder: Binder() {
        fun getService(): BTService = this@BTService
    }

    private val broadcastReceiver = object : BroadcastReceiver() { // ДЕЙСТВИЯ РЕСИВЕРА
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    showProgress()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    hideProgress()
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) // получаем устройство из intent
                    device?.let { addFoundDevice(device) } // добаляем устройство если оно не null
                }
            }
        }
    }

    private fun showProgress() {
        progressBarStateListener?.onShowProgress()
    }

    private fun hideProgress() {
        progressBarStateListener?.onHideProgress()
    }

    private fun addFoundDevice(device: BluetoothDevice) {
        foundDeviceListener?.onFoundDevice(device)
        //Toast.makeText(this, "Найдено устройство: ${device.address}", Toast.LENGTH_SHORT).show()
    }

    private fun sendCommand(string: String) {
        Log.d(TAG, "sendCommand: $string")
        //Toast.makeText(this, "Команда $string", Toast.LENGTH_SHORT).show()
        connectedThread?.write(string)
    }

    private fun parseData(data: String) {

    }

    fun setOnFoundDeviceListener(listener: OnFoundDeviceListener) {
        foundDeviceListener = listener
    }

    fun setOnProgressBarStateListener(listener: OnProgressBarStateListener) {
        progressBarStateListener = listener
    }

    fun startScan() {
        stopScan()
        bluetoothAdapter.startDiscovery()
    }

    fun stopScan() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    fun connectToDevice(macAddress: String) {
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)
        connectThread = ConnectThread(bluetoothDevice)
        connectThread?.run()
    }

    /**
     * Закрытие соединения
     */
    fun closeConnection() {
        connectThread?.cancel()
        connectedThread?.cancel()
    }

    fun down() {
        sendCommand(DOWN)
    }

    fun up() {
        sendCommand(UP)
    }

    fun left() {
        sendCommand(LEFT)
    }

    fun right() {
        sendCommand(RIGHT)
    }

    /**
     * Класс для соединения с устройством
     */
    inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            val method: Method = device.javaClass.getMethod("rfSConne", Int.javaClass)
        }

        override fun run() {
            stopScan()
            mmSocket?.use { socket ->
                socket.connect()
                manageConnectSocket(socket)
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "cancel: Ошибка при закрытии сокета")
            }
        }
    }

    /**
     *
     * Класс для работы  с текущим соединением
     */
    inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private var mmInputStream: InputStream? = null
        private var mmOutputStream: OutputStream? = null
        private var isConnected = false

        init {
            var tmpIS: InputStream? = null
            var tmpOS: OutputStream? = null
            try {
                tmpIS = socket.inputStream
                tmpOS = socket.outputStream
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mmInputStream = tmpIS
            mmOutputStream = tmpOS

            isConnected = true
        }

        override fun run() {
            val buffer = StringBuilder()
            while (isConnected) {
                try {
                    if (mmInputStream?.available()!! > 0) {
                        val readChar = mmInputStream!!.read()
                        buffer.append(readChar.toChar())
                        val eof = buffer.indexOf("\r\n")
                        if (eof > 0) {
                            parseData(buffer.toString())
                            buffer.delete(0, buffer.length)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun write(_data: String) {
            var data = _data
            mmOutputStream?.let {
                try {
                    data += "\n"
                    it.write(data.toByteArray())
                    it.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun cancel() {
            try {
                isConnected = false
                mmInputStream?.close()
                mmOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun manageConnectSocket(socket: BluetoothSocket) {
        Toast.makeText(this, "Соединение успешно!", Toast.LENGTH_SHORT).show()

        connectedThread = ConnectedThread(socket)
        connectedThread?.run()
    }

    companion object {
        const val TAG = "BTService"

        const val UP = "up"
        const val DOWN = "down"
        const val LEFT = "left"
        const val RIGHT = "right"
    }
}

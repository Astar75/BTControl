package com.example.btcontrol

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList
import java.util.HashSet

class MainActivity : AppCompatActivity(), OnFoundDeviceListener, OnProgressBarStateListener {

    private val dialog: BTDeviceDialog? = BTDeviceDialog()
    private var mService: BTService? = null  // здесь будем хранить сервис
    private var mBound = false               // привязан или не привязан сервис

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate: called")

        EnableBluetooth()

        btn_devices.setOnClickListener {
            dialog?.show(supportFragmentManager, "kek414")
        }
        btn_left.setOnClickListener {
            mService?.left()
        }
        btn_right.setOnClickListener {
            mService?.right()
        }
        btn_up.setOnClickListener {
            mService?.up()
        }
        btn_down.setOnClickListener {
            mService?.down()
        }
    }
    /* */ /* */ /* */ /* АХАХХА это  ясделаль :) */ /* */ /* */ /* */
    fun dialogCloser() {
        dialog?.dismiss();
    }



    // переменная которая хранит соединение к сервису (соединение м/у сервисом и активностью)
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(serviceName: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected: serviceName = $serviceName")
            val binder: BTService.LocalBinder = service as BTService.LocalBinder
            mService = binder.getService()
            // чтобы у нас MainActivity получала какие то события от сервиса мы связали
            // сервис и MainActivity вместе через слушатель. Здесь мы передали объект - самого себя (main activity) в сервис
            // потому что MainActivity у нас реализует интерфейс OnFoundDeviceListener -- прослойка между сервисом и активити
            mService?.setOnFoundDeviceListener(this@MainActivity)     // отвечает за поиск устройств
            mService?.setOnProgressBarStateListener(this@MainActivity) // отвечает за показ прогресс бара на экране
            mBound = true
        }

        override fun onServiceDisconnected(serviceName: ComponentName) {
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called")
        // привязываемся к сервису через намерение Intent, передавая тот сервис, который хотим запустить
        // вторым параметром передаем переменную которая будет хранить в себе соединение между сервисом и
        // активностью (не блютуз подключение), третий параметр - мы хотим чтобы при привязке к сервису
        // он так же бы автоматически запускался
        bindService(Intent(this, BTService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
        // отсоединяемся от сервиса и прекращаем ее работу
        unbindService(connection)
    }

    /**
     * Этот метод принадлежит интерфейсу OnFoundDeviceListener
     * BTService представим себе водителем который дергает за рычаг
     * MainActivity то есть данный класс - представим себе двигателем который
     * принимает себе действия водителя.
     *
     * В данном случае сервис нашел устройство и передал его сюда в  активити
     */
    override fun onFoundDevice(device: BluetoothDevice) {
        // активити это устройство уже скармливает диалогу что бы отобразить в списке
        dialog?.addDevice(device)
    }


    /**
     * Методы из интерфейса OnProgressBarStateListener
     * для отображения и скрытия ProgressBar во время сканирования
     * устройств
     */
    override fun onShowProgress() {
        dialog?.showProgress()
    }

    override fun onHideProgress() {
        dialog?.hideProgress()
    }

    fun connectDevice(device: BluetoothDevice) {
        Toast.makeText(this, "Подключен к устройству: " + device.address, Toast.LENGTH_SHORT).show()
        mService?.connectToDevice(device.address)
    }

    fun EnableBluetooth() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(intent, requestBT)
    }

    fun startScan() {
        mService?.startScan()
    }

    fun stopScan() {
        mService?.stopScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestBT)
            if (resultCode == Activity.RESULT_CANCELED) {
                EnableBluetooth()
                Toast.makeText(this, "Эээ! Ты чё? Блютуз включил быстро!", Toast.LENGTH_SHORT).show()
            }
    }


    companion object {
        const val TAG = "MainActivity"
        const val requestBT = 10
    }
}
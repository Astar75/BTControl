package com.example.btcontrol

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog.*
import kotlinx.android.synthetic.main.dialog.view.*

class BTDeviceDialog() : DialogFragment() {

    private val adapter = DeviceAdapter()
    private lateinit var contentView: View
    private var isScanProgress = false


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        contentView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog, null)

        contentView.btn_scan?.setOnClickListener {
            (activity as MainActivity).startScan() // вызываем запуск сканирования у MainActivity
        }

        contentView.btn_stop_scan.setOnClickListener {
            (activity as MainActivity).stopScan() // вызываем остановку сканирования у MainActivity
        }

        contentView.devicesRecycler.adapter = adapter
        contentView.devicesRecycler.setHasFixedSize(true)

        adapter.clickItemListenerInit(object : DeviceAdapter.OnClickItemListener {
            override fun onClickItem(device: BluetoothDevice) {
                (activity as MainActivity).connectDevice(device)
                // TODO: 23.09.2020 СДелАть закрытие диалогового окна и прекратить поиск
                (activity as MainActivity).stopScan() // вызываем остановку сканирования у MainActivity
                (activity as MainActivity).dialogCloser() // закрываем списек
            }
        })

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Поиск устройств")
        alertDialogBuilder.setView(contentView)
        val dialog = alertDialogBuilder.create()
        dialog.setOnShowListener {

        }
        return dialog
    }

    fun showProgress() {
        contentView.scanProgress?.visibility = View.VISIBLE
    }

    fun hideProgress() {
        contentView.scanProgress?.visibility = View.GONE
    }

    /**
     * Добавляем устройство в список найденых
     */
    fun addDevice(device: BluetoothDevice) {
        adapter.addDevice(device)
    }

    /**
     * Удаление списка устройств когда мы нажали поиск
     */
    fun clearDevices() {
        adapter.clearDevice()
    }
}
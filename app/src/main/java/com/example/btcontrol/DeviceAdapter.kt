package com.example.btcontrol

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_device.view.*

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private var clickItemListener: OnClickItemListener? = null

    fun clickItemListenerInit(clickItemListener: OnClickItemListener) {
        this.clickItemListener = clickItemListener
    }

    /**
     * оБРАБОТАТЬ НАЖАТИЕ НА ЭЛЕМЕНТ МОЖНО ЗДЕСЬ
     */

    private val mData = mutableListOf<BluetoothDevice>()

    fun addDevice(device: BluetoothDevice) {
        if (!mData.contains(device)) {
            mData.add(device)
            notifyDataSetChanged() // обновление списка устройств при изменении
        }
    }

    fun clearDevice() {
        mData.clear()
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                clickItemListener?.onClickItem(mData[adapterPosition])
            }
        }

        fun bind(device: BluetoothDevice) {
            // если имя устройства не задано, то внутри скобок мы не отобразим имя на экране.
            // it - это переменная которая в данном случае содержит значение name  которое гарантированно не может быть null
            device.name?.let { itemView.deviceName.text = it }
            itemView.macAddress.text = device.address
        }
    }

    /**
     * интерфейс (методы без реализации)
     */
    interface OnClickItemListener {
        fun onClickItem(device: BluetoothDevice)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceViewHolder { // загружает элемент списка устройств
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_device,
            parent,
            false
        )
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) { // привязка
        holder.bind(mData[position])
    }

    override fun getItemCount() = mData.size // return количество элементов списка устройств
}
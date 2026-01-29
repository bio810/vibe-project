package com.bio810.vibe

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val deviceList = ArrayList<String>()
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        findViewById<ListView>(R.id.deviceList).adapter = listAdapter

        findViewById<Button>(R.id.btnScan).setOnClickListener {
            startNoGpsScan()
        }

        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    private fun startNoGpsScan() {
        if (bluetoothAdapter == null) {
            tvStatus.text = "狀態：此裝置不支援藍牙"
            return
        }

        // 只請求藍牙相關權限，排除定位請求
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        if (permissions.any { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            tvStatus.text = "狀態：正在請求藍牙權限..."
            ActivityCompat.requestPermissions(this, permissions, 1)
        } else {
            if (!bluetoothAdapter!!.isEnabled) {
                tvStatus.text = "狀態：請開啟藍牙開關"
                return
            }
            deviceList.clear()
            listAdapter.notifyDataSetChanged()
            bluetoothAdapter?.startDiscovery()
            tvStatus.text = "狀態：正在搜尋藍牙裝置 (無視 GPS)"
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val name = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) device?.name ?: "未知裝置" else "無權限"
                val info = "$name\n${device?.address}"
                if (!deviceList.contains(info)) {
                    deviceList.add(info)
                    listAdapter.notifyDataSetChanged()
                    tvStatus.text = "狀態：找到 ${deviceList.size} 個裝置"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

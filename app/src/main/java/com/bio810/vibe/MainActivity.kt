package com.bio810.vibe

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
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
            startAdvancedScan()
        }

        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    private fun startAdvancedScan() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        
        if (!gpsEnabled) {
            tvStatus.text = "狀態：請開啟手機 GPS 定位！"
            Toast.makeText(this, "藍牙掃描需要開啟定位", Toast.LENGTH_LONG).show()
            return
        }

        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permissions.any { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            tvStatus.text = "狀態：正在請求權限..."
            ActivityCompat.requestPermissions(this, permissions, 1)
        } else {
            deviceList.clear()
            bluetoothAdapter?.startDiscovery()
            tvStatus.text = "狀態：正在掃描..."
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
                    tvStatus.text = "狀態：已找到 ${deviceList.size} 個裝置"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

package com.bio810.vibe

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val deviceList = ArrayList<String>()
    private lateinit var listAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        val btnScan = findViewById<Button>(R.id.btnScan)
        val listView = findViewById<ListView>(R.id.deviceList)

        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = listAdapter

        btnScan.setOnClickListener {
            checkPermissionsAndScan()
        }

        // 註冊廣播接收器來獲取搜尋到的裝置
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private fun checkPermissionsAndScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        
        deviceList.clear()
        listAdapter.notifyDataSetChanged()
        bluetoothAdapter.startDiscovery()
        Toast.makeText(this, "正在搜尋裝置...", Toast.LENGTH_SHORT).show()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) device?.name else "未知裝置"
                val deviceAddress = device?.address
                if (deviceName != null) {
                    val info = "\n"
                    if (!deviceList.contains(info)) {
                        deviceList.add(info)
                        listAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

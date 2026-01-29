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
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val deviceList = ArrayList<String>()
    private lateinit var listAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化藍牙適配器
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        val btnScan = findViewById<Button>(R.id.btnScan)
        val listView = findViewById<ListView>(R.id.deviceList)

        // 設定清單顯示器
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = listAdapter

        // 按鈕點擊事件
        btnScan.setOnClickListener {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "此裝置不支援藍牙", Toast.LENGTH_SHORT).show()
            } else {
                checkPermissionsAndScan()
            }
        }

        // 註冊廣播接收器來監聽搜尋到的裝置
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private fun checkPermissionsAndScan() {
        // 檢查 Android 12+ 必備的藍牙掃描權限
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        } else {
            startScanning()
        }
    }

    private fun startScanning() {
        deviceList.clear()
        listAdapter.notifyDataSetChanged()
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.startDiscovery()
            Toast.makeText(this, "正在搜尋附近的裝置...", Toast.LENGTH_SHORT).show()
        }
    }

    // 接收搜尋結果的廣播
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    device?.name ?: "未知名稱"
                } else {
                    "缺少權限"
                }
                val deviceAddress = device?.address
                
                val info = "$deviceName\n$deviceAddress"
                if (!deviceList.contains(info)) {
                    deviceList.add(info)
                    listAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

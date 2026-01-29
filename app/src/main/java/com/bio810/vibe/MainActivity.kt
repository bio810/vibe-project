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

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        val btnScan = findViewById<Button>(R.id.btnScan)
        val listView = findViewById<ListView>(R.id.deviceList)

        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = listAdapter

        btnScan.setOnClickListener {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "錯誤：此手機不支援藍牙", Toast.LENGTH_LONG).show()
            } else if (!bluetoothAdapter!!.isEnabled) {
                Toast.makeText(this, "請先開啟手機藍牙功能！", Toast.LENGTH_LONG).show()
            } else {
                checkPermissionsAndScan()
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private fun checkPermissionsAndScan() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            Toast.makeText(this, "正在請求權限...", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        } else {
            startScanning()
        }
    }

    private fun startScanning() {
        deviceList.clear()
        deviceList.add("正在搜尋中，請稍候...") // 先放一行字確認清單有動
        listAdapter.notifyDataSetChanged()
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            val started = bluetoothAdapter?.startDiscovery()
            if (started == true) {
                Toast.makeText(this, "掃描已啟動，請確保周圍有可偵測裝置", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "掃描啟動失敗，請重試", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // 如果找到第一個裝置，就把「正在搜尋中」那行刪掉
                if (deviceList.contains("正在搜尋中，請稍候...")) {
                    deviceList.remove("正在搜尋中，請稍候...")
                }

                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    device?.name ?: "未知名稱"
                } else {
                    "缺少連線權限"
                }
                val deviceAddress = device?.address
                
                val info = "$deviceName ($deviceAddress)"
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

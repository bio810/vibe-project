package com.bio810.vibe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 關鍵：將介面檔連結起來
        setContentView(R.layout.activity_main)
    }
}

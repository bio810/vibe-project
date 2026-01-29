package com.bio810.vibe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 核心指令：告訴 App 顯示剛才建立的黑底紅字畫面
        setContentView(R.layout.activity_main)
    }
}

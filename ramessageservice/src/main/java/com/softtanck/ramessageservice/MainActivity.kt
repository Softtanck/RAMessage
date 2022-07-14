package com.softtanck.ramessageservice

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, RaConnectionService::class.java)
        startService(intent)
//        ContextCompat.startForegroundService(this, intent)
    }
}
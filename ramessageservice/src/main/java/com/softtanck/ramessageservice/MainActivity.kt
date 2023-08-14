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
        val firstIntent = Intent(this, RaConnectionService::class.java)
        startService(firstIntent)
        val secondIntent = Intent(this, RaConnectionServiceV2::class.java)
        startService(secondIntent)
    }
}
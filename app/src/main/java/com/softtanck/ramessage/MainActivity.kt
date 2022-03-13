package com.softtanck.ramessage

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.softtanck.ramessage.`interface`.RaTestInterface
import com.softtanck.ramessageclient.RaClientApi
import com.softtanck.ramessageclient.core.listener.BindStateListener
import com.softtanck.ramessageclient.core.listener.DisconnectedReason

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        RaClientApi.INSTANCE.bindRaConnectionService(this, ComponentName("com.softtanck.ramessageservice", "com.softtanck.ramessageservice.RaConnectionService"), object : BindStateListener {
            override fun connectedToRaServices() {
                Log.d("~~~", "connectedToRaServices: $this")
                val testInterface = RaClientApi.INSTANCE.create(RaTestInterface::class.java)
                val testReturnAModel = testInterface.testReturnAModel("I am from the caller", 1)
                Log.d("~~~", "testReturnAModel:$testReturnAModel")
                val testReturnAllList = testInterface.testReturnAllList("I am from the caller")
                Log.d("~~~", "testReturnAllList:$testReturnAllList")
                testInterface.testVoid()
            }

            override fun onConnectRaServicesFailed() {
                Log.d("~~~", "onConnectRaServicesFailed: ")
            }

            override fun disconnectedFromRaServices(@DisconnectedReason disconnectedReason: Int) {
                Log.d("~~~", "disconnectedFromRaServices: $disconnectedReason")
            }
        })
    }
}
package com.softtanck.ramessage

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.softtanck.ramessage.`interface`.RaTestInterface
import com.softtanck.ramessageclient.RaClientApi
import com.softtanck.ramessageclient.core.listener.BindStateListener
import com.softtanck.ramessageclient.core.listener.DisconnectedReason
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun bind() {
        RaClientApi.INSTANCE.bindRaConnectionService(this, ComponentName("com.softtanck.ramessageservice", "com.softtanck.ramessageservice.RaConnectionService"), object : BindStateListener {
            override fun onConnectedToRaServices() {
                Log.d("~~~", "connectedToRaServices: $this")
                val testInterface = RaClientApi.INSTANCE.create(RaTestInterface::class.java)
//                val testReturnAModel = testInterface.testReturnAModel("I am from the caller", 1)
//                Log.d("~~~", "testReturnAModel:${testReturnAModel?.testString}")
//                val testReturnAllList = testInterface.testReturnAllList("I am from the caller")
//                Log.d("~~~", "testReturnAllList:$testReturnAllList")
//                testInterface.testVoid()
//                val testBoolean = testInterface.testBoolean()
//                Log.d("~~~", "testBoolean: $testBoolean")
                val testString = testInterface.testString()
                Log.d("~~~", "testString: $testString")
                GlobalScope.launch {
                    suspendTestFun()
                }
            }

            override fun onConnectRaServicesFailed() {
                Log.d("~~~", "onConnectRaServicesFailed: ")
//                bind()
            }

            override fun onDisconnectedFromRaServices(@DisconnectedReason disconnectedReason: Int) {
                Log.d("~~~", "disconnectedFromRaServices: $disconnectedReason")
//                bind()
            }
        })
    }

    suspend fun suspendTestFun() {
        val testInterface = RaClientApi.INSTANCE.create(RaTestInterface::class.java)
        val suspendFun = testInterface.suspendFun()
        Log.d("~~~", "suspendTestFun: done,$suspendFun")
//        RaClientApi.INSTANCE.unbindRaConnectionService()
    }

    override fun onResume() {
        super.onResume()
        RaClientApi.INSTANCE.addRemoteBroadcastMessageListener { msg ->
            Log.d("~~~", "onResume broadcast: $msg")
        }
        bind()
    }

    override fun onStop() {
        super.onStop()
        RaClientApi.INSTANCE.unbindRaConnectionService()
        exitProcess(0)
    }
}
package com.softtanck.ramessage

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shared.model.Food
import com.softtanck.ramessage.`interface`.RaTestInterface
import com.softtanck.ramessageclient.RaClientApi
import com.softtanck.ramessageclient.core.listener.BindStateListener
import com.softtanck.ramessageclient.core.listener.DisconnectedReason
import kotlinx.coroutines.Dispatchers
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
                var remoteFood: Food? = null
                // 1. Get a food from other process
                remoteFood = testInterface.getAFood()
                Log.d("~~~", "getAFood result: $remoteFood")

                // 2. Get a food with parameter
                remoteFood = testInterface.getAFoodWithParameter("Banana")
                Log.d("~~~", "getAFoodWithParameter: $remoteFood")

                // 3. Get all foods
                val allFoods = testInterface.getAllFoods()
                Log.d("~~~", "getAllFoods: $allFoods, ${allFoods?.size}")

                // 4. Eat food
                testInterface.eatFood()

                // 5. Buy a food
                val buyFoodResult = testInterface.buyFood()
                Log.d("~~~", "buyFood: $buyFoodResult")

                // 6. Get a food name
                val foodName = testInterface.getFoodName()
                Log.d("~~~", "getFoodName: $foodName")

                // 7. Set food name
                val changedFoodName = testInterface.setFoodName("Pear")
                Log.d("~~~", "setFoodName: $changedFoodName")

                // 8. Suspend
                lifecycleScope.launch(Dispatchers.IO) {

                    // 8.1 buy food
                    val suspendBuyFoodResult = testInterface.suspendBuyFood()
                    Log.d("~~~", "suspendBuyFood: $suspendBuyFoodResult")

                    // 8.2 get food
                    val suspendGetFood = testInterface.suspendGetFood()
                    Log.d("~~~", "suspendGetFood: $suspendGetFood")

                }

            }

            override fun onConnectRaServicesFailed() {
                Log.d("~~~", "onConnectRaServicesFailed: ")
            }

            override fun onDisconnectedFromRaServices(@DisconnectedReason disconnectedReason: Int) {
                Log.d("~~~", "disconnectedFromRaServices: $disconnectedReason")
            }
        })
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
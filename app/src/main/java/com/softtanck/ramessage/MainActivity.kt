package com.softtanck.ramessage

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shared.model.Food
import com.softtanck.ramessage.ipc.RaTestInterface
import com.softtanck.ramessageclient.RaClientApi
import com.softtanck.ramessageclient.core.listener.BindStatusChangedListener
import com.softtanck.ramessageclient.core.listener.DisconnectedReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val componentName = ComponentName("com.softtanck.ramessageservice", "com.softtanck.ramessageservice.RaConnectionService")
    private val componentNameV2 = ComponentName("com.softtanck.ramessageservice", "com.softtanck.ramessageservice.RaConnectionServiceV2")

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_connect_first_service).setOnClickListener {
            RaClientApi.INSTANCE.bindRaConnectionService(this, componentName, object : BindStatusChangedListener {
                override fun onConnectedToRaServices(componentName: ComponentName) {
                    Log.d(TAG, "connectedToRaServices: $this-$componentName")
                    val testInterface = RaClientApi.INSTANCE.create(componentName = componentName, service = RaTestInterface::class.java)
                    // 1. Get a food from other process
                    var remoteFood: Food? = testInterface.getAFood()
                    Log.d(TAG, "getAFood result: $remoteFood")
                    if (remoteFood?.name != "Apple") {
                        throw IllegalStateException("Get a food from other process failed")
                    }

                    // 2. Get a food with parameter
                    remoteFood = testInterface.getAFoodWithParameter("Banana")
                    Log.d(TAG, "getAFoodWithParameter: $remoteFood")

                    // 3. Get all foods
                    val allFoods = testInterface.getAllFoods()
                    Log.d(TAG, "getAllFoods: $allFoods, ${allFoods?.size}")

                    // 4. Eat food
                    testInterface.eatFood()

                    // 5. Buy a food
                    val buyFoodResult = testInterface.buyFood()
                    Log.d(TAG, "buyFood: $buyFoodResult")

                    // 6. Get a food name
                    val foodName = testInterface.getFoodName()
                    Log.d(TAG, "getFoodName: $foodName")

                    // 7. Set food name
                    val changedFoodName = testInterface.setFoodName("Pear")
                    Log.d(TAG, "setFoodName: $changedFoodName")

                    // 8. Suspend
                    lifecycleScope.launch(Dispatchers.IO) {

                        // 8.1 buy food
                        val suspendBuyFoodResult = testInterface.suspendBuyFood()
                        Log.d(TAG, "suspendBuyFood: $suspendBuyFoodResult")

                        // 8.2 get food
                        val suspendGetFood = testInterface.suspendGetFood()
                        Log.d(TAG, "suspendGetFood: $suspendGetFood")

                    }
                }

                override fun onConnectRaServicesFailed(componentName: ComponentName) {
                    Log.d(TAG, "onConnectRaServicesFailed: $componentName")
                }

                override fun onDisconnectedFromRaServices(componentName: ComponentName, @DisconnectedReason disconnectedReason: Int) {
                    Log.d(TAG, "disconnectedFromRaServices: $disconnectedReason-$componentName")
                }
            })
        }

        findViewById<Button>(R.id.btn_connect_second_service).setOnClickListener {
            RaClientApi.INSTANCE.bindRaConnectionService(this, componentNameV2, object : BindStatusChangedListener {
                override fun onConnectedToRaServices(componentName: ComponentName) {
                    Log.d(TAG, "connectedToRaServices: $this-$componentName")
                    val testInterface = RaClientApi.INSTANCE.create(componentName = componentName, service = RaTestInterface::class.java)
                    // 1. Get a food from other process
                    var remoteFood: Food? = testInterface.getAFood()
                    Log.d(TAG, "getAFood result: $remoteFood")
                    if (remoteFood?.name != "AppleV2") {
                        throw IllegalStateException("Get a food from other process failed")
                    }

                    // 2. Get a food with parameter
                    remoteFood = testInterface.getAFoodWithParameter("Banana")
                    Log.d(TAG, "getAFoodWithParameter: $remoteFood")

                    // 3. Get all foods
                    val allFoods = testInterface.getAllFoods()
                    Log.d(TAG, "getAllFoods: $allFoods, ${allFoods?.size}")

                    // 4. Eat food
                    testInterface.eatFood()

                    // 5. Buy a food
                    val buyFoodResult = testInterface.buyFood()
                    Log.d(TAG, "buyFood: $buyFoodResult")

                    // 6. Get a food name
                    val foodName = testInterface.getFoodName()
                    Log.d(TAG, "getFoodName: $foodName")

                    // 7. Set food name
                    val changedFoodName = testInterface.setFoodName("Pear")
                    Log.d(TAG, "setFoodName: $changedFoodName")

                    // 8. Suspend
                    lifecycleScope.launch(Dispatchers.IO) {

                        // 8.1 buy food
                        val suspendBuyFoodResult = testInterface.suspendBuyFood()
                        Log.d(TAG, "suspendBuyFood: $suspendBuyFoodResult")

                        // 8.2 get food
                        val suspendGetFood = testInterface.suspendGetFood()
                        Log.d(TAG, "suspendGetFood: $suspendGetFood")

                    }
                }

                override fun onConnectRaServicesFailed(componentName: ComponentName) {
                    Log.d(TAG, "onConnectRaServicesFailed: $componentName")
                }

                override fun onDisconnectedFromRaServices(componentName: ComponentName, @DisconnectedReason disconnectedReason: Int) {
                    Log.d(TAG, "disconnectedFromRaServices: $disconnectedReason-$componentName")
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        RaClientApi.INSTANCE.addRemoteBroadcastMessageListener(componentName = componentName) { msg ->
            Log.d(TAG, "onResume broadcast: $msg-V1")
        }
        RaClientApi.INSTANCE.addRemoteBroadcastMessageListener(componentName = componentNameV2) { msg ->
            Log.d(TAG, "onResume broadcast: $msg-V2")
        }
    }

    override fun onStop() {
        super.onStop()
        RaClientApi.INSTANCE.destroyAllResources()
        exitProcess(0)
    }
}
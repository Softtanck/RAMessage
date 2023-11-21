package com.softtanck.ramessageservice

import android.os.Message
import android.util.Log
import com.shared.model.Food
import com.softtanck.ramessageservice.ipc.RaTestInterface
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val testFood = Food("AppleV2")

interface MyServerTestFunImplV2 : RaTestInterface {

    override fun getAFood(): Food? {
        Log.d("~~~", "[SERVER] getAFood: Service is invoked V2")
        return testFood
    }

    override fun getAFoodWithParameter(foodName: String): Food? {
        Log.d("~~~", "[SERVER] getAFoodWithParameter: Service is invoked, foodName:$foodName V2")
        return testFood.apply {
            name = foodName
        }
    }

    override fun getAllFoods(): List<Food>? {
        Log.d("~~~", "[SERVER] getAllFoods V2")
        return mutableListOf<Food>().apply {
            repeat(10) {
                add(testFood)
            }
        }
    }

    override fun eatFood() {
        Log.d("~~~", "[SERVER] eatFood V2")
    }

    override fun buyFood(): Boolean {
        Log.d("~~~", "[SERVER] buyFood V2")
        return true
    }

    override fun getFoodName(): String {
        Log.d("~~~", "[SERVER] getFoodName V2")
        return testFood.name
    }

    override fun setFoodName(foodName: String): String {
        Log.d("~~~", "[SERVER] setFoodName: $foodName V2")
        testFood.name = foodName
        return testFood.name
    }

    override suspend fun suspendBuyFood(): Boolean {
        Log.d("~~~", "[SERVER] suspendBuyFood V2")
        return true
    }

    override suspend fun suspendGetFood(): Food {
        Log.d("~~~", "[SERVER] suspendGetFood V2")
        // You will received the broadcast message in client
        RaServerApi.INSTANCE.sendBroadcastToAllClients(serviceKey = RaServerApi.INSTANCE.getAllRaClientServiceKeys().find { it == RaConnectionServiceV2::class.java.name }, message = Message.obtain())
        GlobalScope.launch {
            while (true) {
                delay(5000)
                RaServerApi.INSTANCE.sendBroadcastToAllClients(serviceKey = RaServerApi.INSTANCE.getAllRaClientServiceKeys().find { it == RaConnectionServiceV2::class.java.name }, message = Message.obtain())
            }
        }
        return testFood
    }
}
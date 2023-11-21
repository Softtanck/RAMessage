package com.softtanck.ramessageservice

import android.os.Message
import android.util.Log
import com.shared.model.Food
import com.softtanck.ramessageclient.RaClientApi
import com.softtanck.ramessageservice.ipc.RaTestInterface

/**
 * @author Softtanck
 * @date 2022/3/22
 * Description: TODO
 */

private val testFood = Food("Apple")

interface MyServerTestFunImpl : RaTestInterface {

    override fun getAFood(): Food? {
        Log.d("~~~", "[SERVER] getAFood: Service is invoked")
        return testFood
    }

    override fun getAFoodWithParameter(foodName: String): Food? {
        Log.d("~~~", "[SERVER] getAFoodWithParameter: Service is invoked, foodName:$foodName")
        return testFood.apply {
            name = foodName
        }
    }

    override fun getAllFoods(): List<Food>? {
        Log.d("~~~", "[SERVER] getAllFoods")
        return mutableListOf<Food>().apply {
            repeat(10) {
                add(testFood)
            }
        }
    }

    override fun eatFood() {
        Log.d("~~~", "[SERVER] eatFood")
    }

    override fun buyFood(): Boolean {
        Log.d("~~~", "[SERVER] buyFood")
        return true
    }

    override fun getFoodName(): String {
        Log.d("~~~", "[SERVER] getFoodName")
        return testFood.name
    }

    override fun setFoodName(foodName: String): String {
        Log.d("~~~", "[SERVER] setFoodName: $foodName")
        testFood.name = foodName
        return testFood.name
    }

    override suspend fun suspendBuyFood(): Boolean {
        Log.d("~~~", "[SERVER] suspendBuyFood")
        return true
    }

    override suspend fun suspendGetFood(): Food {
        Log.d("~~~", "[SERVER] suspendGetFood")
        // You will received the broadcast message in client
        RaServerApi.INSTANCE.sendBroadcastToAllClients(serviceKey = RaServerApi.INSTANCE.getAllRaClientServiceKeys().find { it == RaConnectionService::class.java.name }, message = Message.obtain())
        return testFood
    }
}
package com.softtanck.ramessageservice

import android.util.Log
import com.shared.model.Food
import com.softtanck.RaMessageInterface
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
        return testFood.name
    }

    override suspend fun suspendBuyFood(): Boolean {
        Log.d("~~~", "[SERVER] suspendBuyFood")
        return true
    }

    override suspend fun suspendGetFood(): Food {
        Log.d("~~~", "[SERVER] suspendGetFood")
        return testFood
    }
}
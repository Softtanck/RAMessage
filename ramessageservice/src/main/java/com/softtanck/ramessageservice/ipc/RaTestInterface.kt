package com.softtanck.ramessageservice.ipc

import com.shared.model.Food
import com.softtanck.RaMessageInterface

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
interface RaTestInterface : RaMessageInterface {
    fun getAFood(): Food?
    fun getAFoodWithParameter(foodName: String): Food?
    fun getAllFoods(): List<Food>?
    fun eatFood()
    fun buyFood(): Boolean
    fun getFoodName(): String
    fun setFoodName(foodName: String): String

    suspend fun suspendBuyFood(): Boolean?
    suspend fun suspendGetFood(): Food?
}
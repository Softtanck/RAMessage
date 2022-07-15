package com.softtanck.ramessageservice.`interface`

import com.shared.model.Food

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
interface RaTestInterface {
    fun getAFood(): Food?
    fun getAFoodWithParameter(foodName: String): Food?
    fun getAllFoods(): List<Food>?
    fun eatFood()
    fun buyFood(): Boolean
    fun getFoodName(): String
    fun setFoodName(foodName: String): String

    fun suspendBuyFood(): Boolean?
    fun suspendGetFood(): Food?
}
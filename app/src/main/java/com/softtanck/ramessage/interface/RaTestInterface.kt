package com.softtanck.ramessage.`interface`

import com.shared.model.RaTestModel

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
interface RaTestInterface {
//    fun testReturnAModel(testString: String, testNumber: Int): RaTestModel?
//    fun testReturnAllList(testString: String): List<RaTestModel>?
//    fun testVoid()
//    fun testBoolean(): Boolean
    fun testString(): String
//    fun testSendString(testString: String): String
//
    suspend fun suspendFun(): Boolean
}
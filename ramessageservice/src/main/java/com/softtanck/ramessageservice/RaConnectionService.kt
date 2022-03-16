package com.softtanck.ramessageservice

import android.util.Log
import com.shared.model.RaTestModel
import com.softtanck.ramessageservice.`interface`.RaTestInterface

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: Do your logic here at server side.
 * 1. Implementing the interface from client
 * 2. And override the fun
 */
class RaConnectionService : BaseConnectionService(), RaTestInterface {

    override fun testReturnAModel(testString: String, testNumber: Int): RaTestModel {
        Log.d("~~~", "[SERVER] testReturnAModel: Service is invoked, testString:$testString, testNumber:$testNumber")
        return RaTestModel("服务端返回新的ID")
    }

    override fun testReturnAllList(testString: String): List<RaTestModel> {
        Log.d("~~~", "[SERVER] testReturnAllList: Service is invoked")
        return arrayListOf(RaTestModel("新接口返回的服务端返回新的ID"))
    }

    override fun testVoid() {
        Log.d("~~~", "[SERVER] testVoid: Service is invoked")
    }

    override fun testBoolean(): Boolean {
        Log.d("~~~", "testBoolean: ")
        return true
    }

    override fun testString(): String {
        Log.d("~~~", "testString: ")
        return "你好"
    }
}
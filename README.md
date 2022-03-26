# 🔥🔥🔥一个类似Retrofit的IPC通信框架，支持Java、Kotlin以及同步调用、异步调用、协程
一个高可用、高维护、高性能、线程安全的IPC通信框架。（Android全平台支持，仅98kb）
- Kotlin 👍
- Java 👍
- Android 4 - Android 12+ 👍
- 同步调用 👍
- 异步调用 👍
- 线程安全 👍 
- 一个服务端对多客户端 👍
- 双向发送和实现（目前只支持客户端给服务端发送和接受：同步、异步；服务端给客户端发送：异步、同步暂不支持，新的分支在处理。WIP）
- 协程 👍
- 支持接口参数、返回参数为：基本类型或实现了Parcelable或List<out Parcelable> 👍
- 提醒消息 （WIP）  
- 异常机制 （WIP）
- 混淆（WIP）
## 如何使用
### 客户端
- 1. 先在客户端定义想要IPC的接口；
```kotlin
interface RaTestInterface {
    fun testReturnAModel(testString: String, testNumber: Int): RaTestModel?
    fun testReturnAllList(testString: String): List<RaTestModel>?
    fun testVoid()
    fun testBoolean(): Boolean
    fun testString(): String
    fun testSendString(testString: String): String

    // 推荐使用
    suspend fun suspendFun(): Boolean
}
```
- 2. 在客户端绑定远程服务成功后，通过 ```RaClientApi.INSTANCE.create(RaTestInterface::class.java)```方法即可获得对应服务，然后调用对应接口即可；
#### 客户端示例    
```kotlin
// 1. 提供被绑定的远程服务器名字；2. 在绑定成功后，调用远程服务即可；
RaClientApi.INSTANCE.bindRaConnectionService(this, ComponentName("com.softtanck.ramessageservice", "com.softtanck.ramessageservice.RaConnectionService"), object : BindStateListener {
    override fun onConnectedToRaServices() {
        Log.d("~~~", "connectedToRaServices: $this")
        val testInterface = RaClientApi.INSTANCE.create(RaTestInterface::class.java)
        val testReturnAModel = testInterface.testReturnAModel("I am from the caller", 1)
        Log.d("~~~", "testReturnAModel:${testReturnAModel?.testString}")
        val testReturnAllList = testInterface.testReturnAllList("I am from the caller")
        Log.d("~~~", "testReturnAllList:$testReturnAllList")
        testInterface.testVoid()
        GlobalScope.launch {
            suspendTestFun()
        }
    }

    override fun onConnectRaServicesFailed() {
        Log.d("~~~", "onConnectRaServicesFailed: ")
    }

    override fun onDisconnectedFromRaServices(@DisconnectedReason disconnectedReason: Int) {
        Log.d("~~~", "disconnectedFromRaServices: $disconnectedReason")
    }
})

    suspend fun suspendTestFun() {
        val testInterface = RaClientApi.INSTANCE.create(RaTestInterface::class.java)
        val suspendFun = testInterface.suspendFun()
        Log.d("~~~", "suspendTestFun: done,$suspendFun")
    }
```
### 服务端
- 1. 继承```BaseConnectionService```
- 2. 实现```RaTestInterface```接口
#### 服务端示例    
```kotlin
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

    override fun suspendFun(): Boolean {
        Log.d("~~~", "[SERVER] suspendFun: Service is invoked")
        return true
    }
}
```
## 一些说明和TODO
- 推荐使用协程的方式调用，因为协程方式的默认内部走异步逻辑，某些情况性能更佳；
- 非协程且方法带有返回值，默认走同步逻辑，调用在那个线程，任务就在那个线程执行；
- 非协程且方法不带返回值，默认走异步逻辑，远程任务永远在子线程中运行 且 排队；
- 服务端不需要实现suspend；（待讨论）
- 客户端自定义的接口不能被混淆；
- 客户端自定义的参数中带有「对象」或者 返回值是对象，该对象必须实现Parcelable接口 且 「客户端」和「服务端」的定义的该对象的「包名」一致；
- 客户端自定义的参数中的对象或返回值对象不能被混淆；
- 接口带有返回值是「同步」调用，不带返回值是「异步」调用；
- 如果项目支持 协程，无论是否带返回值的接口都支持「异步」调用；
- 当接口带有返回值时，调用方需要考虑调用的线程防止出现ANR。
## 注意（参数或返回值为基本类型【包含String】**无需关心**）
- 当参数是对象的时候，该对象必须实现Parcelable接口；
- 当客户端期望的接口的返回值是对象的时候，该对象必须实现Parcelable接口；
- 接口如果有返回值，但是如果远程调用失败，返回值为空，请注意「**空指针**」异常；
例如：
该接口```fun testReturnAModel(testString: String, testNumber: Int): RaTestModel```中的```RaTestModel```需要实现Parcelable，且服务端和客户端都需要定义**相同包名**的类；
# Licence
```
Copyright 2022 Softtanck.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

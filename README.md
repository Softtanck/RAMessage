# 🔥🔥🔥A lightweight cross-process communication component on Android, Kotlin and Coroutines both supported.

A lightweight cross-process communication component on Android。（All versions are supported，only 98kb）![RUNOOB 图标](https://jitpack.io/v/Softtanck/RAMessage.svg)

|           Type           | Supported |
| :----------------------: | :-------: |
| All versions for Android |     ✅     |
|          Kotlin          |     ✅     |
| Sync call |     ✅     |
|          Async Call          |     ✅     |
| Coroutines |     ✅     |
|          Thread-Safe          |     ✅     |
| Many(Client)-To-Many(Service) |     ✅     |
|          Method's Parameters：1、Basic type；2、Object which is implemented Parcelable；3、`List<out Parcelable>`；4、`List<out String>`；5、`List<out Int>`；6、`List<out Charsequence>`          |     ✅     |
| Broadcast message |     ✅     |
| Proguard |     ✅     |

## How to use

```kotlin
implementation 'com.github.Softtanck:RAMessage:2.0.0-RC3'  
```

### Client

1.  Defined the interface at client side；

```kotlin
interface RaTestInterface : IRaMessageInterface {  
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
```

2.  Use method of `RaClientApi.INSTANCE.create(RaTestInterface::class.java)` after bind is successful；

#### Client simple

```kotlin
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
```

### Service

1.  extend `BaseConnectionService`
2.  Implement `RaTestInterface`

#### Service sample

```kotlin
interface MyServerTestFunImpl : RaTestInterface {

    override fun getAFood(): Food? {
        Log.d(TAG, "[SERVER] getAFood: Service is invoked")
        return testFood
    }

    override fun getAFoodWithParameter(foodName: String): Food? {
        Log.d(TAG, "[SERVER] getAFoodWithParameter: Service is invoked, foodName:$foodName")
        return testFood.apply {
            name = foodName
        }
    }

    override fun getAllFoods(): List<Food>? {
        Log.d(TAG, "[SERVER] getAllFoods")
        return mutableListOf<Food>().apply {
            repeat(10) {
                add(testFood)
            }
        }
    }

    override fun eatFood() {
        Log.d(TAG, "[SERVER] eatFood")
    }

    override fun buyFood(): Boolean {
        Log.d(TAG, "[SERVER] buyFood")
        return true
    }

    override fun getFoodName(): String {
        Log.d(TAG, "[SERVER] getFoodName")
        return testFood.name
    }

    override fun setFoodName(foodName: String): String {
        Log.d(TAG, "[SERVER] setFoodName: $foodName")
        return testFood.name
    }

    override suspend fun suspendBuyFood(): Boolean {
        Log.d(TAG, "[SERVER] suspendBuyFood")
        return true
    }

    override suspend fun suspendGetFood(): Food {
        Log.d(TAG, "[SERVER] suspendGetFood")
        return testFood
    }
}
```

# Proguard

    -keep class * extends com.softtanck.IRaMessageInterface { *;}  
    -keep interface * extends com.softtanck.IRaMessageInterface { *;}  
    -keep class com.softtanck.ramessageclient.core.engine.retrofit.RemoteServiceMethod { *; }  
    -keep class com.softtanck.ramessageservice.** { *; }  

# 🔥🔥🔥一个高性能且线程安全的IPC通信框架，支持Java、Kotlin以及同步调用、异步调用、协程

一个高性能且线程安全的IPC通信框架。（Android全平台支持，仅98kb）![RUNOOB 图标](https://jitpack.io/v/Softtanck/RAMessage.svg)

|           Type           | Supported |
| :----------------------: | :-------: |
| Android所有版本 |     ✅     |
|          Kotlin          |     ✅     |
| 同步调用 |     ✅     |
|          异步调用          |     ✅     |
| 协程 |     ✅     |
|          线程安全         |     ✅     |
| 多个客户端<->多个服务端 |     ✅     |
|支持接口参数、返回参数为：1、基本类型；2、实现了Parcelable的对象；3、```List<out Parcelable>```；4、```List<out String>```；5、```List<out Int>```；6、```List<out Charsequence>```|     ✅     |
| 广播消息 |     ✅     |
| 混淆 |     ✅     |

## 如何使用

```kotlin
implementation 'com.github.Softtanck:RAMessage:2.0.0-RC3'  
```

### 客户端

1.  先在客户端定义想要IPC的接口；

```kotlin
interface RaTestInterface : IRaMessageInterface {  
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
```

2.  在客户端绑定远程服务成功后，通过 `RaClientApi.INSTANCE.create(ComponentName, RaTestInterface::class.java)`方法即可获得对应服务，然后调用对应接口即可；

#### 客户端示例

```kotlin
// 1. 提供被绑定的远程服务器名字；2. 在绑定成功后，调用远程服务即可；  
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
```
### 服务端
1. 继承```BaseConnectionService```
2. 实现```RaTestInterface```接口
#### 服务端示例
```kotlin
interface MyServerTestFunImpl : RaTestInterface {

    override fun getAFood(): Food? {
        Log.d(TAG, "[SERVER] getAFood: Service is invoked")
        return testFood
    }

    override fun getAFoodWithParameter(foodName: String): Food? {
        Log.d(TAG, "[SERVER] getAFoodWithParameter: Service is invoked, foodName:$foodName")
        return testFood.apply {
            name = foodName
        }
    }

    override fun getAllFoods(): List<Food>? {
        Log.d(TAG, "[SERVER] getAllFoods")
        return mutableListOf<Food>().apply {
            repeat(10) {
                add(testFood)
            }
        }
    }

    override fun eatFood() {
        Log.d(TAG, "[SERVER] eatFood")
    }

    override fun buyFood(): Boolean {
        Log.d(TAG, "[SERVER] buyFood")
        return true
    }

    override fun getFoodName(): String {
        Log.d(TAG, "[SERVER] getFoodName")
        return testFood.name
    }

    override fun setFoodName(foodName: String): String {
        Log.d(TAG, "[SERVER] setFoodName: $foodName")
        return testFood.name
    }

    override suspend fun suspendBuyFood(): Boolean {
        Log.d(TAG, "[SERVER] suspendBuyFood")
        return true
    }

    override suspend fun suspendGetFood(): Food {
        Log.d(TAG, "[SERVER] suspendGetFood")
        return testFood
    }
}
```
## 一些说明
- 推荐使用协程的方式调用；
- 自定义的参数中的对象或函数返回值对象不能被混淆；
- 自定义的参数是对象时，该对象必须实现Parcelable接口；
- 接口带有返回值是「同步」调用，不带返回值是「异步」调用；
- 如果项目支持 协程，无论是否带返回值的接口都支持「异步」调用；
- 当接口带有返回值时，调用方需要考虑调用同步方法的时候的线程防止出现ANR（协程不需要考虑）；
- 接口如果有返回值，但是如果远程调用失败，返回值为空，请注意「**空指针**」异常；

# 混淆
```
-keep class * extends com.softtanck.IRaMessageInterface { *;}
-keep interface * extends com.softtanck.IRaMessageInterface { *;}
-keep class com.softtanck.ramessageclient.core.engine.retrofit.RemoteServiceMethod { *; }
-keep class com.softtanck.ramessageservice.** { *; }
```
# Licence
```
Copyright 2023 Softtanck.
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

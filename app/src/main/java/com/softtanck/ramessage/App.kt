package com.softtanck.ramessage

import android.app.Application
import android.content.Context
import me.weishu.reflection.Reflection

/**
 * @author Softtanck
 * @date 2022/3/25
 * Description: TODO
 */
class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // 由于HOOK了Handler中的Binder去实现同步。但是Android P+限制了Hide函数的访问。注意：只有服务端需要，客户端不需要；
        // TODO : 当无法hook的时候，服务端将采用默认的Binder返回，目前未处理：会抛出异常
//        Reflection.unseal(base)
    }
}
package com.softtanck.ramessageclient.core.engine.retrofit

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class RaRetrofit(private val validateEagerly: Boolean) {
    private val serviceMethodCache: MutableMap<Method, ServiceMethod<*>?> = ConcurrentHashMap()

    // Single-interface proxy creation guarded by parameter safety.
    fun <T> create(service: Class<T>): T {
        validateServiceInterface(service)
        return Proxy.newProxyInstance(
            service.classLoader, arrayOf<Class<*>>(service),
            object : InvocationHandler {
                private val emptyArgs = Array<Any>(0) {}

                @Throws(Throwable::class)
                override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
                    // If the method is a method from Object then defer to normal invocation.
                    if (method.declaringClass == Any::class.java) {
                        return method.invoke(this, *args!!)
                    }
                    val platform = Platform.get()
                    return if (platform.isDefaultMethod(method)) platform.invokeDefaultMethod(method, service, proxy, *(args ?: emptyArgs)) else loadServiceMethod(method)!!.invoke(args)
                }
            }) as T
    }

    private fun validateServiceInterface(service: Class<*>) {
        require(service.isInterface) { "API declarations must be interfaces." }
        val check: Deque<Class<*>> = ArrayDeque(1)
        check.add(service)
        while (!check.isEmpty()) {
            val candidate = check.removeFirst()
            if (candidate.typeParameters.isNotEmpty()) {
                val message = StringBuilder("Type parameters are unsupported on ").append(candidate.name)
                if (candidate != service) {
                    message.append(" which is an interface of ").append(service.name)
                }
                throw IllegalArgumentException(message.toString())
            }
            Collections.addAll(check, *candidate.interfaces)
        }
        if (validateEagerly) {
            val platform = Platform.get()
            for (method in service.declaredMethods) {
                if (!platform.isDefaultMethod(method) && !Modifier.isStatic(method.modifiers)) {
                    loadServiceMethod(method)
                }
            }
        }
    }

    fun loadServiceMethod(method: Method): ServiceMethod<*>? {
        var result = serviceMethodCache[method]
        if (result != null) return result
        synchronized(serviceMethodCache) {
            result = serviceMethodCache[method]
            if (result == null) {
                result = ServiceMethod.parseAnnotations<Any>(method)
                serviceMethodCache[method] = result
            }
        }
        return result
    }
}
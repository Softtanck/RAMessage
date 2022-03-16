package com.softtanck.ramessageclient.core.engine.retrofit;


import java.lang.reflect.Method;
import java.lang.reflect.Type;

import kotlin.coroutines.Continuation;

final class RequestFactory {
    public static RequestFactory parseAnnotations(RaRetrofit retrofit, Method method) {
        return new Builder(retrofit, method).build();
    }

    final RaRetrofit retrofit;
    final Method method;
    final Type[] parameterTypes;
    final boolean isKotlinSuspendFunction;

    RequestFactory(Builder builder) {
        retrofit = builder.retrofit;
        method = builder.method;
        parameterTypes = builder.parameterTypes;
        isKotlinSuspendFunction = builder.isKotlinSuspendFunction;
    }

    static final class Builder {
        final RaRetrofit retrofit;
        final Method method;
        final Type[] parameterTypes;
        boolean isKotlinSuspendFunction;

        Builder(RaRetrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;
            this.parameterTypes = method.getGenericParameterTypes();
        }

        RequestFactory build() {
            for (Type parameterType : parameterTypes) {
                try {
                    if (Utils.getRawType(parameterType) == Continuation.class) {
                        isKotlinSuspendFunction = true;
                        break;
                    }
                } catch (NoClassDefFoundError ignored) {
                    // Ignored
                }
            }
            return new RequestFactory(this);
        }
    }
}

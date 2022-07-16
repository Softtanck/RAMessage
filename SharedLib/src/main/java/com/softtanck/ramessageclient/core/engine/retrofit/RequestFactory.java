package com.softtanck.ramessageclient.core.engine.retrofit;


import com.softtanck.util.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import kotlin.coroutines.Continuation;

final class RequestFactory {
    public static RequestFactory parseAnnotations(Method method) {
        return new Builder(method).build();
    }

    final Method method;
    final Type[] parameterTypes;
    final boolean isKotlinSuspendFunction;

    RequestFactory(Builder builder) {
        method = builder.method;
        parameterTypes = builder.parameterTypes;
        isKotlinSuspendFunction = builder.isKotlinSuspendFunction;
    }

    static final class Builder {
        final Method method;
        final Type[] parameterTypes;
        boolean isKotlinSuspendFunction;

        Builder(Method method) {
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

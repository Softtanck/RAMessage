package com.softtanck.ramessageclient.core.engine.retrofit;

import android.util.Log;

import androidx.annotation.Nullable;

import com.softtanck.model.RaRequestTypeArg;
import com.softtanck.model.RaRequestTypeParameter;
import com.softtanck.ramessageclient.RaClientApi;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * This method is copied from retrofit, And this class will be changed later.
 * TODO : Remove unused parameters, and the Coroutines will be supported later. -Softtanck
 */
abstract class RemoteServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {

    private static final String TAG = RemoteServiceMethod.class.getSimpleName();

    static <ResponseT, ReturnT> RemoteServiceMethod<ResponseT, ReturnT> parseAnnotations(
            RaRetrofit retrofit, Method method, RequestFactory requestFactory) {
        return new CallAdapted<>(method);
    }

    protected abstract @Nullable
    ReturnT adapt(Object[] args);

    @Nullable
    @Override
    ReturnT invoke(Object[] args) {
        return adapt(args);
    }

    static final class CallAdapted<ResponseT, ReturnT> extends RemoteServiceMethod<ResponseT, ReturnT> {

        private final Method method;
        private final ArrayList<RaRequestTypeParameter> parameters = new ArrayList<>();
        private final ArrayList<RaRequestTypeArg> argsList = new ArrayList<>();

        CallAdapted(Method method) {
            this.method = method;
        }

        @Override
        protected ReturnT adapt(@Nullable Object[] args) {
            synchronized (parameters) {
                parameters.clear();
                for (Class<?> parameterType : method.getParameterTypes()) {
                    parameters.add(new RaRequestTypeParameter(parameterType));
                }
            }
            synchronized (argsList) {
                argsList.clear();
                if (args != null) {
                    for (Object arg : args) {
                        argsList.add(new RaRequestTypeArg(arg));
                    }
                }
            }
            Log.d(TAG, "[CLIENT] Start the remote methods, methodName:" + method.getName() + ", parameters.size:" + parameters.size() + ", argsList.size:" + argsList.size());
            if (method.getGenericReturnType().equals(Void.TYPE)) {
                RaClientApi.getINSTANCE().remoteMethodCallAsync(method.getName(), parameters, argsList);
                return null;
            } else {
                Object returnValue = RaClientApi.getINSTANCE().remoteMethodCallSync(method.getName(), parameters, argsList);
                if (returnValue != null) {
                    return (ReturnT) returnValue;
                } else {
                    return null;
                }
            }
        }
    }

    private static Class<?> boxIfPrimitive(Class<?> type) {
        if (boolean.class == type) return Boolean.class;
        if (byte.class == type) return Byte.class;
        if (char.class == type) return Character.class;
        if (double.class == type) return Double.class;
        if (float.class == type) return Float.class;
        if (int.class == type) return Integer.class;
        if (long.class == type) return Long.class;
        if (short.class == type) return Short.class;
        return type;
    }
}

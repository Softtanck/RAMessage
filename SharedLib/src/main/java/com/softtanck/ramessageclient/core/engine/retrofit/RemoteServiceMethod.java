package com.softtanck.ramessageclient.core.engine.retrofit;

import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.softtanck.model.RaRequestTypeArg;
import com.softtanck.model.RaRequestTypeParameter;
import com.softtanck.ramessageclient.RaClientApi;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import kotlin.coroutines.Continuation;

/**
 * This method is copied from retrofit, And this class will be changed later.
 * TODO : Remove unused parameters, and the Coroutines will be supported later. -Softtanck
 */
abstract class RemoteServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {

    private static final String TAG = RemoteServiceMethod.class.getSimpleName();

    static <ResponseT, ReturnT> RemoteServiceMethod<ResponseT, ReturnT> parseAnnotations(
            RaRetrofit retrofit, Method method, RequestFactory requestFactory) {
        boolean isKotlinSuspendFunction = requestFactory.isKotlinSuspendFunction;
        boolean continuationWantsResponse = false;
        boolean methodHasReturnValue = Utils.hasReturnValueType(method, method.getGenericReturnType());

        if (isKotlinSuspendFunction) {
            Type[] parameterTypes = method.getGenericParameterTypes();
            Type responseType = Utils.getParameterLowerBound(0, (ParameterizedType) parameterTypes[parameterTypes.length - 1]);
            methodHasReturnValue = Utils.hasReturnValueType(method, responseType);
            if (methodHasReturnValue) {
                continuationWantsResponse = true;
            } else {
                // TODO figure out if type is nullable or not
                // Metadata metadata = method.getDeclaringClass().getAnnotation(Metadata.class)
                // Find the entry for method
                // Determine if return type is nullable or not
            }
        }
        if (isKotlinSuspendFunction) {
            return new SuspendAdapted<>(method, continuationWantsResponse);
        } else {
            return new CallAdapted<>(method, methodHasReturnValue);
        }
    }

    protected abstract @Nullable
    ReturnT adapt(Object[] args);

    @Nullable
    @Override
    ReturnT invoke(Object[] args) {
        return adapt(args);
    }

    static final class SuspendAdapted<ResponseT, ReturnT, T extends Parcelable> extends RemoteServiceMethod<ResponseT, ReturnT> {

        private final Method method;
        private final Boolean methodHasReturnValue;
        private final ArrayList<RaRequestTypeParameter> parameters = new ArrayList<>();
        private final ArrayList<T> argsList = new ArrayList<>();

        SuspendAdapted(Method method, Boolean methodHasReturnValue) {
            this.method = method;
            this.methodHasReturnValue = methodHasReturnValue;
        }

        @Nullable
        @Override
        protected ReturnT adapt(Object[] args) {
            //noinspection unchecked Checked by reflection inside RequestFactory.
            Continuation<T> continuation = (Continuation<T>) args[args.length - 1];

            synchronized (parameters) {
                parameters.clear();
                for (Class<?> parameterType : method.getParameterTypes()) {
                    if (Utils.getRawType(parameterType) == Continuation.class) continue;
                    parameters.add(new RaRequestTypeParameter(parameterType));
                }
            }
            synchronized (argsList) {
                argsList.clear();
                for (Object arg : args) {
                    if (arg instanceof Continuation) continue;
                    if (arg instanceof Parcelable) argsList.add((T) arg);
                    else argsList.add((T) new RaRequestTypeArg(arg));
                }
            }
            Log.d(TAG, "[CLIENT] Start the remote methods, methodName:" + method.getName() + ", parameters.size:" + parameters.size() + ", argsList.size:" + argsList.size());
            // See SuspendForBody for explanation about this try/catch.
            try {
                return (ReturnT) KotlinExtensions.awaitResponse(method.getName(), parameters, argsList, continuation);
            } catch (Exception e) {
                return (ReturnT) KotlinExtensions.suspendAndThrow(e, continuation);
            }
        }
    }

    static final class CallAdapted<ResponseT, ReturnT, T extends Parcelable> extends RemoteServiceMethod<ResponseT, ReturnT> {

        private final Method method;
        private final Boolean methodHasReturnValue;
        private final ArrayList<RaRequestTypeParameter> parameters = new ArrayList<>();
        private final ArrayList<T> argsList = new ArrayList<>();

        CallAdapted(Method method, Boolean methodHasReturnValue) {
            this.method = method;
            this.methodHasReturnValue = methodHasReturnValue;
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
                        if (arg instanceof Parcelable) argsList.add((T) arg);
                        else argsList.add((T) new RaRequestTypeArg(arg));
                    }
                }
            }
            Log.d(TAG, "[CLIENT] Start the remote methods, methodName:" + method.getName() + ", parameters.size:" + parameters.size() + ", argsList.size:" + argsList.size());
            if (!methodHasReturnValue) {
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

    private static boolean isBoxIfPrimitive(Class<?> type) {
        if (boolean.class == type) return true;
        if (byte.class == type) return true;
        if (char.class == type) return true;
        if (double.class == type) return true;
        if (float.class == type) return true;
        if (int.class == type) return true;
        if (long.class == type) return true;
        return short.class == type;
    }
}

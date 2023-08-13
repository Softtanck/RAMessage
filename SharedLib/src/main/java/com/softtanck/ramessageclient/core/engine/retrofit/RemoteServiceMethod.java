package com.softtanck.ramessageclient.core.engine.retrofit;

import android.content.ComponentName;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.softtanck.model.RaRequestTypeArg;
import com.softtanck.model.RaRequestTypeParameter;
import com.softtanck.ramessageclient.RaClientApi;
import com.softtanck.util.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import kotlin.coroutines.Continuation;

/**
 * This method is copied from retrofit, And this class will be changed later.
 * TODO : Remove unused parameters, and the Coroutines will be supported later. -Softtanck
 */
abstract class RemoteServiceMethod<ReturnT> extends ServiceMethod<ReturnT> {

    private static final String TAG = RemoteServiceMethod.class.getSimpleName();

    static <ReturnT> RemoteServiceMethod<ReturnT> parseAnnotations(ComponentName componentName, Method method, RequestFactory requestFactory) {
        boolean isKotlinSuspendFunction = requestFactory.isKotlinSuspendFunction;
        boolean methodHasReturnValue = Utils.hasReturnValueType(method, method.getGenericReturnType());

        if (isKotlinSuspendFunction) {
            Type[] parameterTypes = method.getGenericParameterTypes();
            Type responseType = Utils.getParameterLowerBound(0, (ParameterizedType) parameterTypes[parameterTypes.length - 1]);
            methodHasReturnValue = Utils.hasReturnValueType(method, responseType);
        }
        if (isKotlinSuspendFunction) {
            return new SuspendAdapted<>(componentName, method);
        } else {
            return new CallAdapted<>(componentName, method, methodHasReturnValue);
        }
    }

    protected abstract @Nullable ReturnT adapt(Object[] args);

    @Nullable
    @Override
    ReturnT invoke(Object[] args) {
        return adapt(args);
    }

    static final class SuspendAdapted<ReturnT, T extends Parcelable> extends RemoteServiceMethod<ReturnT> {

        private final Method method;
        private final ComponentName componentName;

        SuspendAdapted(ComponentName componentName, Method method) {
            this.method = method;
            this.componentName = componentName;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        protected ReturnT adapt(Object[] args) {
            //noinspection unchecked Checked by reflection inside RequestFactory.
            Continuation<T> continuation = (Continuation<T>) args[args.length - 1];

            final ArrayList<RaRequestTypeParameter> parameters = new ArrayList<>();
            final ArrayList<T> argsList = new ArrayList<>();

            for (Class<?> parameterType : method.getParameterTypes()) {
                if (Utils.getRawType(parameterType) == Continuation.class) parameters.add(new RaRequestTypeParameter(Continuation.class));
                else parameters.add(new RaRequestTypeParameter(parameterType));
            }
            for (Object arg : args) {
                if (arg instanceof Continuation) continue;
                if (arg instanceof Parcelable) argsList.add((T) arg);
                else argsList.add((T) new RaRequestTypeArg(arg));
            }
            Log.d(TAG, "[CLIENT] Start the remote methods, methodName:" + method.getName() + ", parameters.size:" + parameters.size() + ", argsList.size:" + argsList.size());
            // See SuspendForBody for explanation about this try/catch.
            try {
                return (ReturnT) KotlinExtensions.awaitResponse(componentName, method.getName(), parameters, argsList, continuation);
            } catch (Exception e) {
                return (ReturnT) KotlinExtensions.suspendAndThrow(e, continuation);
            }
        }
    }

    static final class CallAdapted<ReturnT, T extends Parcelable> extends RemoteServiceMethod<ReturnT> {

        private final Method method;
        private final ComponentName componentName;
        private final Boolean methodHasReturnValue;
        private final ArrayList<RaRequestTypeParameter> parameters = new ArrayList<>();
        private final ArrayList<T> argsList = new ArrayList<>();

        CallAdapted(ComponentName componentName, Method method, Boolean methodHasReturnValue) {
            this.method = method;
            this.methodHasReturnValue = methodHasReturnValue;
            this.componentName = componentName;
        }

        @SuppressWarnings("unchecked")
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
                RaClientApi.getINSTANCE().remoteMethodCallAsync(componentName, method.getName(), parameters, argsList);
                return null;
            } else {
                Object returnValue = RaClientApi.getINSTANCE().remoteMethodCallSync(componentName, method.getName(), parameters, argsList);
                if (returnValue != null) {
                    return (ReturnT) returnValue;
                } else {
                    return null;
                }
            }
        }
    }
}

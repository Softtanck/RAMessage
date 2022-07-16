-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep class * extends com.softtanck.IRaMessageInterface { *;}
-keep interface * extends com.softtanck.IRaMessageInterface { *;}
-keep class com.softtanck.ramessageclient.core.engine.retrofit.RemoteServiceMethod { *; }
-keep class com.softtanck.ramessageservice.** { *; }
@file:Suppress("UNCHECKED_CAST")

package ru.nemodev.platform.core.extensions

import java.lang.reflect.ParameterizedType

fun <T : Any> Class<T>.unwrapCompanionClass(): Class<*> = when {
    name.endsWith("\$Companion") -> enclosingClass ?: this
    else -> this
}

fun <F : Any> Any.getPrivateField(fieldName: String): F? {
    return javaClass.getPrivateField(this, fieldName)
}

fun <F : Any> Any.getSupperClassPrivateField(fieldName: String): F? {
    return javaClass.superclass.getPrivateField(this, fieldName)
}

fun <F : Any> Any.setPrivateField(fieldName: String, value: F?) {
    javaClass.setPrivateField(this, fieldName, value)
}

fun <F : Any> Any.setSuperClassPrivateField(fieldName: String, value: F?) {
    javaClass.superclass.setPrivateField(this, fieldName, value)
}

fun <F : Any> Any.mutatePrivateField(fieldName: String, replace: (F?) -> F?): F? {
    return javaClass.mutatePrivateField(this, fieldName, replace)
}

fun <F : Any> Class<*>.mutatePrivateField(obj: Any, fieldName: String, replace: (F?) -> F?): F? {
    val oldField = getPrivateField<F>(obj, fieldName)
    val replacement = replace(oldField)
    setPrivateField(obj, fieldName, replacement)
    return oldField
}

fun <F : Any> Class<*>.getPrivateField(obj: Any, fieldName: String): F? {
    return with(getDeclaredField(fieldName)) {
            isAccessible = true
            get(obj) as? F
    }
}

fun <F : Any> Class<*>.setPrivateField(obj: Any, fieldName: String, value: F?) {
    return with(getDeclaredField(fieldName)) {
        isAccessible = true
        set(obj, value)
    }
}

fun Class<*>.getFieldClass(fieldName: String): Class<*> {
    return getDeclaredField(fieldName).type
}

fun Any.getFieldClass(fieldName: String): Class<*> {
    return javaClass.getDeclaredField(fieldName).type
}

fun Any.getSuperClassFieldClass(fieldName: String): Class<*> {
    return javaClass.superclass.getDeclaredField(fieldName).type
}

fun Any.getGenericParameterClass(parameterIndex: Int): Class<*> {
    return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[parameterIndex] as Class<*>
}
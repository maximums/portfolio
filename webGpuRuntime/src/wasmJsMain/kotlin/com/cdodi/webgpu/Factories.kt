package com.cdodi.webgpu

import kotlin.js.JsAny

@JsFun("() => ({})")
external fun createEmptyJsObject(): JsAny

inline fun <T : JsAny> createJsObject(config: T.() -> Unit = {}): T {
    return createEmptyJsObject().unsafeCast<T>().apply(config)
}

//@JsFun("(obj, key, value) => { obj[key] = value; }")
//internal external fun setJsProperty(obj: JsAny, key: JsString, value: JsAny?)
//
//fun Any.toJs(): JsAny? {
//    return when (this) {
//        is String -> toJsString()
//        is Boolean -> toJsBoolean()
//        is Byte, is Short -> toInt().toJsNumber()
//        is Int -> toJsNumber()
//        is Float, is Long -> toDouble().toJsNumber()
//        is Double -> toJsNumber()
//        is JsAny -> this
//        is List<*> -> map { it?.toJs() }.toJsArray()
//        // Handle Maps (Records) dynamically
//        is Map<*, *> -> (this as Map<String, Any?>).toJsRecord()
//
//        else -> throw IllegalArgumentException("Cannot convert type ${this::class.simpleName} to JsAny")
//    }
//}
//
//fun Map<String, Any?>.toJsRecord(): JsAny {
//    val jsObject = createEmptyJsObject()
//    val empty = emptyList<String>().toJsArray()
//
//    for ((key, value) in this) {
//        val jsKey = key.toJsString()
//        val jsValue = value.toJsPrimitive()
//
//        setJsProperty(jsObject, jsKey, jsValue)
//    }
//
//    return jsObject
//}

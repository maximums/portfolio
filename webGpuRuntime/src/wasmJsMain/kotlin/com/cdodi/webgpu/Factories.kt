package com.cdodi.webgpu

import kotlin.js.JsAny

@JsFun("() => ({})")
external fun createEmptyJsObject(): JsAny

inline fun <T : JsAny> createJsObject(config: T.() -> Unit): T {
    return createEmptyJsObject().unsafeCast<T>().apply(config)
}

package com.cdodi.transpiler

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT

fun generateKotlin(
    context: BindingContext,
    generatedPackageName: String,
    apiFileName: String,
    factoriesFileName: String,
): List<FileSpec> {
    val fileComment = "Current file is generated, please don't modify it manually because your changes will be lost."
    val fileAnnotation = AnnotationSpec.builder(Suppress::class).addMember(
        "%S, %S, %S, %S, %S",
        "Unused",
        "RedundantVisibilityModifier",
        "RemoveRedundantBackticks",
        "ObjectPropertyName",
        "RemoveRedundantQualifierName"
    ).build()
    val apiFileBuilder = FileSpec.builder(generatedPackageName, apiFileName)
        .addFileComment(fileComment)
        .addAnnotation(fileAnnotation)
    val factoriesFileBuilder = FileSpec.builder(generatedPackageName, factoriesFileName)
        .addFileComment(fileComment)
        .addAnnotation(fileAnnotation)

    context[BindingSlices.ENUM]?.values?.forEach { enumDesc ->
        val enumInterface = enumDesc.asEnumPoet()
        val enumEntries = enumDesc.enumFactory(generatedPackageName)
        apiFileBuilder.addType(enumInterface)
        factoriesFileBuilder.addType(enumEntries)
    }

    context[BindingSlices.INTERFACE]?.values?.forEach { interfaceDesc ->
        val interfaceSpec = interfaceDesc.asInterfacePoet(context, generatedPackageName)
        apiFileBuilder.addType(interfaceSpec)
    }

    context[BindingSlices.DICTIONARY]?.values?.forEach { dictDesc ->
        val dictInterface = dictDesc.asDictionaryPoet(context, generatedPackageName)
//        val dictFactory = dictDesc.dictFactory(context, generatedPackageName)
        apiFileBuilder.addType(dictInterface)
//        fileBuilder.addFunction(dictFactory)
    }

    return listOf(apiFileBuilder.build(), factoriesFileBuilder.build())
}

fun Descriptor.TypeDescriptor.asPoetJs(context: BindingContext, generatedPackageName: String): TypeName = when {
    context[BindingSlices.INTERFACE, name] != null
            || context[BindingSlices.DICTIONARY, name] != null
            || context[BindingSlices.ENUM, name] != null -> ClassName(
        generatedPackageName,
        name
    )

    promiseOf != null -> promiseOf.asPoetJs(context, generatedPackageName).asPromise

    unionMembers.isNotEmpty() -> {
        val iface = unionMembers.joinToString(separator = "Or") { it.name }
        val name = context[BindingSlices.INTERFACE, iface] ?: context[BindingSlices.DICTIONARY, iface]
        name?.let { ClassName(generatedPackageName, it.name) } ?: ClassName("kotlin.js", "JsAny")
    }

    sequenceOf != null -> sequenceOf.asPoetJs(context, generatedPackageName).asArray

    else -> mapPrimitiveJsType()
}

private fun Descriptor.TypeDescriptor.mapPrimitiveJsType(): TypeName =
    when (name) {
        "byte", "octet",
        "short", "unsignedshort",
        "long", "unsignedlong", "longlong", "unsignedlonglong",
        "float", "unrestrictedfloat",
        "double", "unrestricteddouble" -> ClassName("kotlin.js", "JsNumber")
        "boolean" -> ClassName("kotlin.js", "JsBoolean")
        "DOMString", "USVString", "ByteString" -> ClassName("kotlin.js", "JsString")
        else -> ClassName("kotlin.js", "JsAny")
    }.copy(nullable = true)

private val TypeName.asPromise: TypeName
    get() = ClassName("kotlin.js", "Promise").parameterizedBy(this)

private val TypeName.asArray: TypeName
    get() = ClassName("kotlin.js", "JsArray").parameterizedBy(this)

fun Descriptor.TypeDescriptor.asPoetKt(context: BindingContext, generatedPackageName: String): TypeName = when {
    context[BindingSlices.INTERFACE, name] != null
            || context[BindingSlices.DICTIONARY, name] != null
            || context[BindingSlices.ENUM, name] != null -> ClassName(
        generatedPackageName,
        name
    )

    promiseOf != null -> {
        val type = promiseOf.asPoetKt(context, generatedPackageName)
        ClassName("kotlinx.coroutines", "Deferred").parameterizedBy(type)
    }

    unionMembers.isNotEmpty() -> {
        val iface = unionMembers.joinToString(separator = "Or") { it.name }
        val name = context[BindingSlices.INTERFACE, iface] ?: context[BindingSlices.DICTIONARY, iface] // Need to remove `DICTIONARY` check because mark interface is added only to `INTERFACE`
        name?.let { ClassName(generatedPackageName, it.name) } ?: ANY
    }

    sequenceOf != null -> {
        val type = sequenceOf.asPoetKt(context, generatedPackageName)
        ClassName("kotlin.collections", "MutableList").parameterizedBy(type)
    }

    record != null -> {
        val keyType = record.keys.first().asPoetKt(context, generatedPackageName)
        val valueType = record.values.first().asPoetKt(context, generatedPackageName)
        ClassName("kotlin.collections", "MutableMap").parameterizedBy(keyType, valueType)
    }

    else -> mapPrimitiveKtType()
}

fun Descriptor.TypeDescriptor.mapPrimitiveKtType(): TypeName =
    when (name) {
        "byte", "octet" -> BYTE
        "short", "unsignedshort" -> INT
        "long", "unsignedlong", "longlong", "unsignedlonglong" -> LONG
        "float", "unrestrictedfloat" -> FLOAT
        "double", "unrestricteddouble" -> DOUBLE
        "boolean" -> BOOLEAN
        "DOMString", "USVString", "ByteString" -> STRING
        "void", "undefined" -> UNIT
        else -> ANY
    }.copy(nullable = isNullable)

val TypeName.conversionBridge: MemberName?
    get() = when (this) {
        BOOLEAN -> MemberName("kotlin.js", "toJsBoolean")
        STRING -> MemberName("kotlin.js", "toJsString")
        INT, FLOAT, DOUBLE, BYTE, LONG -> MemberName("kotlin.js", "toJsNumber")
        UNIT, ANY -> MemberName("kotlin.js", "toJsUnit")
        else -> null
    }
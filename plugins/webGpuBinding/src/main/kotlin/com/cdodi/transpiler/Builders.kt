package com.cdodi.transpiler

import com.cdodi.transpiler.BindingSlices.CUSTOM_TYPE
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT

fun Descriptor.InterfaceDescriptor.asPoet(context: BindingContext, generatedPackageName: String): TypeSpec {
    val builder = if (isAbstractClass) {
        TypeSpec.classBuilder(name).addModifiers(KModifier.ABSTRACT)
    } else {
        TypeSpec.interfaceBuilder(name)
    }

    builder.addModifiers(KModifier.EXTERNAL)

    val variables = members.filterIsInstance<InterfaceMember.VariableDescriptor>().map { variable ->
        variable.asPoet(context, generatedPackageName)
    }
    val functions = members.filterIsInstance<InterfaceMember.FunctionDescriptor>().map { function ->
        val parameters = function.parameters.map { param ->
            ParameterSpec.builder(param.name, param.type.mapPrimitiveType(context, generatedPackageName))
                .also { if (param.defaultValue != null) it.defaultValue(param.defaultValue) }
                .build()
        }
        if (function.name == "constructor") {
            FunSpec.constructorBuilder().addParameters(parameters).build()
        } else {
            FunSpec.builder(function.name)
                .returns(function.returnType.mapPrimitiveType(context, generatedPackageName))
                .addParameters(parameters)
                .build()
        }
    }

    return builder.addProperties(variables).addFunctions(functions).build()
}

fun InterfaceMember.VariableDescriptor.asPoet(context: BindingContext, generatedPackageName: String) =
    PropertySpec.builder(name, type.mapPrimitiveType(context, generatedPackageName))
        .also { if (defaultValue != null) it.initializer("%S", defaultValue) }
        .build()


fun Descriptor.TypeDescriptor.mapPrimitiveType(context: BindingContext, generatedPackageName: String): TypeName =
    when (name) {
        "byte", "octet" -> BYTE
        "short", "unsignedshort" -> INT
        "long", "unsignedlong", "longlong", "unsignedlonglong" -> LONG
        "float", "unrestrictedfloat" -> FLOAT
        "double", "unrestricteddouble" -> DOUBLE
        "boolean" -> BOOLEAN
        "DOMString", "USVString", "ByteString" -> STRING
        "void", "undefined" -> UNIT
        else -> if (context[CUSTOM_TYPE, name] != null) ClassName(generatedPackageName, name) else ANY
    }.copy(nullable = isNullable)
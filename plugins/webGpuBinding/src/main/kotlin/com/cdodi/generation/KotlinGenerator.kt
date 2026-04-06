package com.cdodi.generation

import WebIDLBaseVisitor
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class KotlinGenerator : WebIDLBaseVisitor<TypeSpec?>() {

    val generatedTypes = mutableListOf<TypeSpec>()
    val generatedFunctions = mutableListOf<FunSpec>()

    private val jsAnyClassName = ClassName("kotlin.js", "JsAny")
    private val jsArrayClassName = ClassName("kotlin.js", "JsArray")
    private val createJsObjectMember = MemberName("com.cdodi.webgpu", "createJsObject")
    private val generatedPackageName = "com.cdodi.webgpu.bindings"

    override fun visitInterfaceRest(ctx: WebIDLParser.InterfaceRestContext?): TypeSpec? {
        if (ctx == null) return null

        // 1. Grab the interface name
        val classNameString = ctx.IDENTIFIER_WEBIDL()?.text ?: return null
        if (!classNameString.startsWith("GPU", ignoreCase = true)) return null

        // 2. Build the external interface
        val interfaceBuilder = TypeSpec.interfaceBuilder(classNameString)
            .addModifiers(KModifier.EXTERNAL)
            .addSuperinterface(jsAnyClassName)

        // 3. Traverse the recursive interfaceMembers list
        var currentMembersCtx = ctx.interfaceMembers()

        while (currentMembersCtx != null && currentMembersCtx.childCount > 0) {
            val interfaceMemberCtx = currentMembersCtx.interfaceMember()
            val partialMemberCtx = interfaceMemberCtx?.partialInterfaceMember()

            // --- HANDLE ATTRIBUTES (Properties) ---
            // Dig down the AST depending on whether it is 'readonly' or not
            val attributeRestCtx = partialMemberCtx?.readonlyMember()?.readonlyMemberRest()?.attributeRest()
                ?: partialMemberCtx?.readWriteAttribute()?.attributeRest()

            if (attributeRestCtx != null) {
                val propertyName = attributeRestCtx.attributeName()?.text
                // Extract the type string (e.g., 'unsignedlong', 'GPUBuffer')
                val typeNameText = attributeRestCtx.typeWithExtendedAttributes()?.type_()?.text

                if (propertyName != null && typeNameText != null) {
                    val kotlinType = mapWebIdlTypeToKotlin(typeNameText)

                    // If it came from the readonlyMember branch, it should be a 'val' in Kotlin
                    val isReadOnly = partialMemberCtx?.readonlyMember() != null

                    interfaceBuilder.addProperty(
                        PropertySpec.builder(propertyName, kotlinType)
                            .mutable(!isReadOnly) // val if readonly, var if read-write
                            .build()
                    )
                }
            }

            // Move to the next member in the AST chain
            currentMembersCtx = currentMembersCtx.interfaceMembers()
        }

        val finalInterface = interfaceBuilder.build()
        generatedTypes.add(finalInterface)

        return finalInterface
    }

    override fun visitDictionary(ctx: WebIDLParser.DictionaryContext): TypeSpec? {
        val classNameString = ctx.IDENTIFIER_WEBIDL()?.text ?: return null
        if (!classNameString.startsWith("GPU", ignoreCase = true)) return null

        val className = ClassName(generatedPackageName, classNameString)

        val interfaceBuilder = TypeSpec.interfaceBuilder(classNameString)
            .addModifiers(KModifier.EXTERNAL)
            .addSuperinterface(jsAnyClassName)

        val factoryFunBuilder = FunSpec.builder(classNameString)
            .addModifiers(KModifier.INLINE)
            .returns(className)

        // Start the apply block using the runtime utility
        factoryFunBuilder.beginControlFlow("return %M<%T>", createJsObjectMember, className)

        var currentMembersCtx = ctx.dictionaryMembers()

        while (currentMembersCtx != null && currentMembersCtx.childCount > 0) {
            val memberCtx = currentMembersCtx.dictionaryMember()
            val restCtx = memberCtx?.dictionaryMemberRest()

            if (restCtx != null) {
                val propertyName = restCtx.IDENTIFIER_WEBIDL()?.text
                val isRequired = restCtx.typeWithExtendedAttributes() != null
                val typeCtx = restCtx.type_() ?: restCtx.typeWithExtendedAttributes()?.type_()
                val typeNameText = typeCtx?.text

                if (propertyName != null && typeNameText != null) {
                    val baseKotlinType = mapWebIdlTypeToKotlin(typeNameText)
                    val kotlinType = if (isRequired) baseKotlinType else baseKotlinType.copy(nullable = true)

                    // Add mutable (var) property to the interface
                    interfaceBuilder.addProperty(
                        PropertySpec.builder(propertyName, kotlinType)
                            .mutable(true)
                            .build()
                    )

                    // Add parameter to the factory function
                    val paramBuilder = ParameterSpec.builder(propertyName, kotlinType)
                    if (!isRequired) {
                        paramBuilder.defaultValue("null")
                    }
                    factoryFunBuilder.addParameter(paramBuilder.build())

                    // Add assignment logic inside the apply block (using `this`)
                    if (isRequired) {
                        factoryFunBuilder.addStatement("this.%L = %L", propertyName, propertyName)
                    } else {
                        factoryFunBuilder.addStatement("if (%L != null) this.%L = %L", propertyName, propertyName, propertyName)
                    }
                }
            }
            currentMembersCtx = currentMembersCtx.dictionaryMembers()
        }

        // Close the apply block
        factoryFunBuilder.endControlFlow()

        val finalInterface = interfaceBuilder.build()
        generatedTypes.add(finalInterface)

        // Only generate the factory if the dictionary actually has properties
        if (finalInterface.propertySpecs.isNotEmpty()) {
            generatedFunctions.add(factoryFunBuilder.build())
        }

        return finalInterface
    }

    private fun mapWebIdlTypeToKotlin(idlType: String): TypeName {
        val isSequence = idlType.startsWith("sequence<")
        val mappedType = when (val cleanType = idlType.removePrefix("sequence<").removeSuffix(">").removeSuffix("?")) {
            "double", "float", "unrestricteddouble", "unrestrictedfloat" -> DOUBLE
            // Wasm numbers require 32-bit types for IDL longs
            "long", "unsignedlong", "short", "unsignedshort" -> INT
            "boolean" -> BOOLEAN
            "DOMString", "USVString", "ByteString" -> STRING
            else -> if (cleanType.startsWith("GPU")) {
                ClassName(generatedPackageName, cleanType)
            } else {
                jsAnyClassName // Fallback to JsAny
            }
        }

        return if (isSequence) {
            jsArrayClassName.parameterizedBy(mappedType)
        } else {
            mappedType
        }
    }
}

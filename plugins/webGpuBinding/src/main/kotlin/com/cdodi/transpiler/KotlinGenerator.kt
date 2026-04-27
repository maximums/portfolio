package com.cdodi.transpiler

import WebIDLBaseVisitor
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.*

private val jsAnyClassName = ClassName("kotlin.js", "JsAny")
private val jsArrayClassName = ClassName("kotlin.js", "JsArray")
private val createJsObjectMember = MemberName("com.cdodi.webgpu", "createJsObject")
private val jsStringClassName = ClassName("kotlin.js", "JsString")
private val jsNumberClassName = ClassName("kotlin.js", "JsNumber")
private val jsBooleanClassName = ClassName("kotlin.js", "JsBoolean")
private val jsPromiseClassName = ClassName("kotlin.js", "Promise")

//class KotlinGenerator(
//    private val bindingContext: BindingContext,
//    private val typeResolver: TypeResolver,
//    private val generatedPackageName: String,
//) : WebIDLBaseVisitor<List<TypeSpec>>() {
//
//    fun buildFileSpec(tree: WebIDLParser.WebIDLContext, fileName: String = "WebGpuBindings"): FileSpec {
//        val fileBuilder = FileSpec.builder(generatedPackageName, fileName)
//        val allGeneratedTypes = visit(tree)
//
//        allGeneratedTypes.forEach { typeSpec -> fileBuilder.addType(typeSpec) }
//
//        return fileBuilder.build()
//    }
//
//    override fun defaultResult(): List<TypeSpec> = emptyList()
//
//    override fun aggregateResult(aggregate: List<TypeSpec>, nextResult: List<TypeSpec>) = aggregate + nextResult
//
//    override fun visitInterfaceRest(ctx: WebIDLParser.InterfaceRestContext): List<TypeSpec> {
//        val name = ctx.IDENTIFIER_WEBIDL()?.text?.trim() ?: return super.visitInterfaceRest(ctx)
//        val target = bindingContext[BindingSlices.INTERFACE, name] ?: return super.visitInterfaceRest(ctx)
//        val partial = bindingContext[BindingSlices.PARTIAL_INTERFACE, name]
//        val result = target + partial
////        val typeSpec = result.asPoet(bindingContext, generatedPackageName)
//
//        return listOf(typeSpec)
//    }
//
//    override fun visitEnum_(ctx: WebIDLParser.Enum_Context): List<TypeSpec> {
//        val enumName = ctx.IDENTIFIER_WEBIDL()?.text?.trim() ?: return super.visitEnum_(ctx)
//        val enumEntries = ctx.enumValueList()?.text.orEmpty()
//            .split(",")
//            .filterNot(predicate = String::isBlank)
//            .map { entry -> entry.trim().removeSurrounding(delimiter = "\"") }
//        val enum = TypeSpec.objectBuilder(enumName)
//            .addModifiers(KModifier.INTERNAL)
//            .also { builder ->
//                enumEntries.forEach { entry ->
//                    val property = PropertySpec.builder(name = entry, type = STRING, KModifier.CONST, KModifier.INTERNAL)
//                        .initializer("%S", entry)
//                        .build()
//                    builder.addProperty(propertySpec = property)
//                }
//            }.build()
//
//        return listOf(enum)
//    }
//
//    override fun visitDictionary(ctx: WebIDLParser.DictionaryContext): List<TypeSpec> {
//        val name = ctx.IDENTIFIER_WEBIDL()?.text?.trim() ?: return super.visitDictionary(ctx)
//        val target = bindingContext[BindingSlices.DICTIONARY, name] ?: return super.visitDictionary(ctx)
//        val partial = bindingContext[BindingSlices.PARTIAL_DICTIONARY, name]
//        val result = target + partial
//        val typeSpec = result.asPoet(bindingContext, generatedPackageName)
//
//        return listOf(typeSpec)
//    }

//    override fun visitTypedef_(ctx: WebIDLParser.Typedef_Context): List<TypeSpec> {
//        val name = ctx.IDENTIFIER_WEBIDL()?.text ?: return super.visitTypedef_(ctx)
//        val typeDescriptor = typeResolver.visit(ctx) ?: return super.visitTypedef_(ctx)
//        val type = typeDescriptor.mapPrimitiveType(bindingContext, generatedPackageName)
//        val typeSpec = TypeAliasSpec.builder(name, type).build()
//        return listOf(typeSpec)
//    }
//}

//private fun WebIDLParser.ArgumentListContext.addConstructor(builder: TypeSpec.Builder) {
//    argument()?.argumentRest()?.let { arg ->
//        val argType = arg.type_()?.let { typeResolver.visit(it) } ?: return
//        val argName = arg.argumentName()?.IDENTIFIER_WEBIDL()?.text?.trim() ?: return
//        val property = PropertySpec.builder(name = argName, type = argType.name).build()
//        builder.addProperty()
//        println("<<<<<<<<<<<<<<<<<<<<<<<<$argType - $argName")
//    }
//    var arguments = arguments()
//    while (arguments != null) {
//        val argument = arguments.argument()?.argumentRest()
//        val argType = argument?.type_()?.let { typeResolver.visit(it) }
//        val argName = argument?.argumentName()?.IDENTIFIER_WEBIDL()?.text?.trim()
//        println(">>>>>>>>>>>>>>>>>>$argType - $argName")
//        arguments = arguments.arguments()
//    }
//}

typealias myLong = Long
package com.cdodi.transpiler

import WebIDLBaseVisitor
import com.cdodi.transpiler.BindingSlices.CUSTOM_TYPE
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.*

private val jsAnyClassName = ClassName("kotlin.js", "JsAny")
private val jsArrayClassName = ClassName("kotlin.js", "JsArray")
private val createJsObjectMember = MemberName("com.cdodi.webgpu", "createJsObject")
private val jsStringClassName = ClassName("kotlin.js", "JsString")
private val jsNumberClassName = ClassName("kotlin.js", "JsNumber")
private val jsBooleanClassName = ClassName("kotlin.js", "JsBoolean")
private val jsPromiseClassName = ClassName("kotlin.js", "Promise")

class KotlinGenerator(
    private val bindingContext: BindingContext,
    private val typeResolver: TypeResolver,
    private val generatedPackageName: String,
) : WebIDLBaseVisitor<TypeSpec?>() {

    override fun visitIncludesStatement(ctx: WebIDLParser.IncludesStatementContext): TypeSpec? {
        val targetClassName = ctx.IDENTIFIER_WEBIDL(0)?.text ?: return super.visitIncludesStatement(ctx)
        val mixinName = ctx.IDENTIFIER_WEBIDL(1)?.text ?: return super.visitIncludesStatement(ctx)
        val targetClass = bindingContext[BindingSlices.INTERFACE, targetClassName] ?: return super.visitIncludesStatement(ctx)
        val mixin = bindingContext[BindingSlices.FAKE_INTERFACE, mixinName]
        val result = targetClass + mixin

        return result.asPoet(bindingContext, generatedPackageName)
    }

    override fun visitInterfaceRest(ctx: WebIDLParser.InterfaceRestContext): TypeSpec? {
        val name = ctx.IDENTIFIER_WEBIDL()?.text?.trim() ?: return super.visitInterfaceRest(ctx)
        val target = bindingContext[BindingSlices.INTERFACE, name] ?: return super.visitInterfaceRest(ctx)
        val partial = bindingContext[BindingSlices.PARTIAL_INTERFACE, name]
        val result = target + partial

        return result.asPoet(bindingContext, generatedPackageName)
    }

    override fun visitEnum_(ctx: WebIDLParser.Enum_Context): TypeSpec? {
        val enumName = ctx.IDENTIFIER_WEBIDL()?.text?.trim() ?: return super.visitEnum_(ctx)
        val enumEntries = ctx.enumValueList()?.text.orEmpty()
            .split(",")
            .filterNot(predicate = String::isBlank)
            .map { entry -> entry.trim().removeSurrounding(delimiter = "\"") }
        val enum = TypeSpec.objectBuilder(enumName)
            .addModifiers(KModifier.INTERNAL)
            .also { builder ->
                enumEntries.forEach { entry ->
                    val property = PropertySpec.builder(name = entry, type = STRING, KModifier.CONST, KModifier.INTERNAL)
                        .initializer("%S", entry)
                        .build()
                    builder.addProperty(propertySpec = property)
                }
            }.build()

        return enum
    }
}

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
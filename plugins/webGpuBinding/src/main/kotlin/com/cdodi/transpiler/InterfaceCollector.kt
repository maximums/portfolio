package com.cdodi.transpiler

import WebIDLBaseVisitor
import WebIDLParser
import org.antlr.v4.runtime.RuleContext

class InterfaceCollector(private val typeResolver: TypeResolver) : WebIDLBaseVisitor<Set<InterfaceMember>>() {
    override fun defaultResult(): Set<InterfaceMember> = emptySet()

    override fun aggregateResult(aggregate: Set<InterfaceMember>, nextResult: Set<InterfaceMember>) = aggregate + nextResult

    override fun visitPartialInterfaceMember(ctx: WebIDLParser.PartialInterfaceMemberContext): Set<InterfaceMember> {
        ctx.readonlyMember()?.readonlyMemberRest()?.attributeRest()?.extractVariable()?.let { return it }
        ctx.readWriteAttribute()?.attributeRest()?.extractVariable()?.let { return it }
        ctx.operation()?.regularOperation()?.extractFunction()?.let { return it }

        return super.visitPartialInterfaceMember(ctx)
    }

    override fun visitMixinMember(ctx: WebIDLParser.MixinMemberContext): Set<InterfaceMember> {
        ctx.attributeRest()?.extractVariable()?.let { return it }
        ctx.regularOperation()?.extractFunction()?.let { return it }

        return super.visitMixinMember(ctx)
    }

    override fun visitInterfaceMember(ctx: WebIDLParser.InterfaceMemberContext): Set<InterfaceMember> {
        ctx.constructor()?.let { constructorCtx ->
            val parameters = constructorCtx.argumentList()?.extractArguments().orEmpty()

            var parentNode: RuleContext? = ctx.parent
            var interfaceName = "Unknown"
            while (parentNode != null) {
                if (parentNode is WebIDLParser.InterfaceRestContext) {
                    interfaceName = parentNode.IDENTIFIER_WEBIDL()?.text?.trim() ?: "Unknown"
                    break
                }
                parentNode = parentNode.parent
            }

            return setOf(
                InterfaceMember.FunctionDescriptor(
                    name = "constructor",
                    returnType = Descriptor.TypeDescriptor(name = interfaceName, isNullable = false),
                    parameters = parameters,
                )
            )
        }

        return super.visitInterfaceMember(ctx)
    }

    override fun visitDictionaryMemberRest(ctx: WebIDLParser.DictionaryMemberRestContext): Set<InterfaceMember> {
        val name = ctx.IDENTIFIER_WEBIDL()?.text?.trim() ?: return super.visitDictionaryMemberRest(ctx)
        val typeCtx = ctx.typeWithExtendedAttributes() ?: ctx.type_()
        val type = typeCtx?.let { typeResolver.visit(it) } ?: return super.visitDictionaryMemberRest(ctx)
        val defaultValue = ctx.default_()?.cleanDefValue

        // (Optional) For now
        // val isRequired = ctx.getChild(0)?.text == "required"

        return setOf(
            InterfaceMember.VariableDescriptor(
                name = name,
                type = type,
                defaultValue = defaultValue // For now disable it, but in general default value will be used only for factory methods
            )
        )
    }

    private fun WebIDLParser.AttributeRestContext.extractVariable(): Set<InterfaceMember>? {
        val attrName = attributeName()?.IDENTIFIER_WEBIDL()?.text?.trim() ?: return null
        val attrType = typeWithExtendedAttributes()?.let { typeResolver.visit(it) } ?: return null

        return setOf(InterfaceMember.VariableDescriptor(name = attrName, type = attrType))
    }

    private fun WebIDLParser.RegularOperationContext.extractFunction(): Set<InterfaceMember>? {
        val returnType = type_()?.let { typeResolver.visit(it) } ?: return null
        val funName = operationRest()?.optionalOperationName()?.operationName()
            ?.IDENTIFIER_WEBIDL()?.text?.trim() ?: return null

        val parameters = operationRest()?.argumentList()?.extractArguments().orEmpty()

        return setOf(
            InterfaceMember.FunctionDescriptor(
                name = funName,
                returnType = returnType,
                parameters = parameters
            )
        )
    }

    private fun WebIDLParser.ArgumentListContext.extractArguments(): Set<InterfaceMember.VariableDescriptor> {
        val collectedArgs = mutableSetOf<InterfaceMember.VariableDescriptor>()

        argument()?.argumentRest()?.toAstMember()?.let(collectedArgs::add)

        var argsCtx = arguments()
        while (argsCtx != null) {
            argsCtx.argument()?.argumentRest()?.toAstMember()?.let(collectedArgs::add)
            argsCtx = argsCtx.arguments()
        }

        return collectedArgs
    }

    private fun WebIDLParser.ArgumentRestContext.toAstMember(): InterfaceMember.VariableDescriptor? {
        val typeCtx = type_() ?: typeWithExtendedAttributes()
        val type = typeCtx?.let { typeResolver.visit(it) } ?: return null

        return InterfaceMember.VariableDescriptor(
            name = argumentName()?.IDENTIFIER_WEBIDL()?.text?.trim().orEmpty(),
            type = type,
            defaultValue = default_()?.cleanDefValue
        )
    }

    private val WebIDLParser.Default_Context.cleanDefValue: String?
        get() {
            val rawText = defaultValue()?.text?.trim() ?: return null
            return rawText.removeSurrounding("\"")
        }
}

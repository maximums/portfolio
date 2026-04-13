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
            defaultValue = default_()?.defaultValue()?.text?.trim()
        )
    }
}

//    override fun visitPartialInterfaceMembers(ctx: WebIDLParser.PartialInterfaceMembersContext): Set<InterfaceMember> {
//        val members = mutableSetOf<InterfaceMember>()
//
//        ctx.partialInterfaceMember()
//            ?.let { visit(it) }
//            ?.let { members += it }
//
//        var membersCtx = ctx.partialInterfaceMembers()
//        while (membersCtx != null) {
//            membersCtx.partialInterfaceMember()
//                ?.let { visit(it) }
//                ?.let { members += it }
//
//            membersCtx = membersCtx.partialInterfaceMembers()
//        }
//
//        return members
//    }
//
//    override fun visitPartialInterfaceMember(ctx: WebIDLParser.PartialInterfaceMemberContext): Set<InterfaceMember> {
//        ctx.readonlyMember()?.let { valContext ->
//            valContext.readonlyMemberRest()?.attributeRest()?.let { attrContext ->
//                val attrName = attrContext.name ?: return super.visitPartialInterfaceMember(ctx)
//                val attrType = attrContext.type ?: return super.visitPartialInterfaceMember(ctx)
//
//                return setOf(InterfaceMember.VariableDescriptor(name = attrName, type = attrType))
//            }
//        }
//        ctx.readWriteAttribute()?.let { varContext ->
//            varContext.attributeRest()?.let { attrContext ->
//                val attrName = attrContext.name ?: return super.visitPartialInterfaceMember(ctx)
//                val attrType = attrContext.type ?: return super.visitPartialInterfaceMember(ctx)
//
//                return setOf(InterfaceMember.VariableDescriptor(name = attrName, type = attrType))
//            }
//        }
//        ctx.operation()?.let { operationsContext ->
//            // Let's hope we will not have special operations (┬┬﹏┬┬)
//            operationsContext.regularOperation()?.let { functionContext ->
//                val returnType = functionContext.type_()?.let { typeResolver.visit(it) } ?: return super.visitPartialInterfaceMember(ctx)
//                val funName = functionContext.operationRest()?.optionalOperationName()?.operationName()
//                    ?.IDENTIFIER_WEBIDL()?.text?.trim() ?: return super.visitPartialInterfaceMember(ctx)
//                val argsCtx = functionContext.operationRest()?.argumentList()
//                val arg = argsCtx?.argument()?.argumentRest()?.toAstMember()
//                val collectedArgs = mutableSetOf<InterfaceMember.VariableDescriptor>().apply { arg?.let(::add) }
//
//                var args = argsCtx?.arguments()
//                while (args != null) {
//                    val argDescriptor = args.argument()?.argumentRest()?.toAstMember()
//                    argDescriptor?.let(collectedArgs::add)
//                    args = args.arguments()
//                }
//
//                return setOf(
//                    InterfaceMember.FunctionDescriptor(
//                        name = funName,
//                        returnType = returnType,
//                        parameters = collectedArgs
//                    )
//                )
//            }
//        }
//        return super.visitPartialInterfaceMember(ctx)
//    }
//
//    override fun visitMixinMembers(ctx: WebIDLParser.MixinMembersContext): Set<InterfaceMember> {
//        val members = mutableSetOf<InterfaceMember>()
//
//        ctx.mixinMember()
//            ?.let { visit(it) }
//            ?.let { members += it }
//
//        var membersCtx = ctx.mixinMembers()
//        while (membersCtx != null) {
//            membersCtx.mixinMember()
//                ?.let { visit(it) }
//                ?.let { members += it }
//
//            membersCtx = membersCtx.mixinMembers()
//        }
//
//        return members
//    }
//
//    override fun visitMixinMember(ctx: WebIDLParser.MixinMemberContext): Set<InterfaceMember> {
//        ctx.attributeRest()?.let { variableContext ->
//            val attrName = variableContext.name ?: return super.visitMixinMember(ctx)
//            val attrType = variableContext.type ?: return super.visitMixinMember(ctx)
//
//            return setOf(InterfaceMember.VariableDescriptor(name = attrName, type = attrType))
//        }
//        ctx.regularOperation()?.let { functionContext ->
//            val returnType = functionContext.type_()?.let { typeResolver.visit(it) } ?: return super.visitMixinMember(ctx)
//            val funName = functionContext.operationRest()?.optionalOperationName()?.operationName()
//                ?.IDENTIFIER_WEBIDL()?.text?.trim() ?: return super.visitMixinMember(ctx)
//            val argsCtx = functionContext.operationRest()?.argumentList()
//            val arg = argsCtx?.argument()?.argumentRest()?.toAstMember()
//            val collectedArgs = mutableSetOf<InterfaceMember.VariableDescriptor>().apply { arg?.let(::add) }
//
//            var args = argsCtx?.arguments()
//            while (args != null) {
//                val argDescriptor = args.argument()?.argumentRest()?.toAstMember()
//                argDescriptor?.let(collectedArgs::add)
//                args = args.arguments()
//            }
//
//            return setOf(
//                InterfaceMember.FunctionDescriptor(
//                    name = funName,
//                    returnType = returnType,
//                    parameters = collectedArgs
//                )
//            )
//        }
//
//        return super.visitMixinMember(ctx)
//    }
//
//    private val WebIDLParser.AttributeRestContext.name: String?
//        get() = attributeName()?.IDENTIFIER_WEBIDL()?.text?.trim()
//
//    private val WebIDLParser.AttributeRestContext.type: Descriptor.TypeDescriptor?
//        get() = typeWithExtendedAttributes()?.let { typeResolver.visit(it) }
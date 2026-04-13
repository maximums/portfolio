package com.cdodi.transpiler

import WebIDLBaseVisitor
import WebIDLParser

class SymbolCollectorVisitor(
    private val context: MutableBidingContext,
    private val membersCollector: InterfaceCollector,
) : WebIDLBaseVisitor<Unit>() {

    override fun visitMixinRest(ctx: WebIDLParser.MixinRestContext) {
        val type = ctx.IDENTIFIER_WEBIDL()?.text?.trim() ?: return
        context[BindingSlices.CUSTOM_TYPE, type] = type

        val collectedMembers = ctx.mixinMembers()?.let { membersCollector.visit(it) }.orEmpty()
        val descriptor = Descriptor.InterfaceDescriptor(
            name = type,
            members = collectedMembers,
            superTypes = emptySet() // Mixin doesn't support inheritance
        )
        val maybePresent = context[BindingSlices.FAKE_INTERFACE, type] // Actually I don't think it is possible, but I am tired already
        context[BindingSlices.FAKE_INTERFACE, type] = descriptor + maybePresent
    }

    override fun visitPartialInterfaceRest(ctx: WebIDLParser.PartialInterfaceRestContext) {
        val type = ctx.IDENTIFIER_WEBIDL()?.text?.trim() ?: return
        context[BindingSlices.CUSTOM_TYPE, type] = type

        val collectedMembers = ctx.partialInterfaceMembers()?.let { membersCollector.visit(it) }.orEmpty()
        val descriptor = Descriptor.InterfaceDescriptor(
            name = type,
            members = collectedMembers,
            superTypes = emptySet() // Partial interfaces doesn't support inheritance either
        )
        val maybePresent = context[BindingSlices.PARTIAL_INTERFACE, type]
        context[BindingSlices.PARTIAL_INTERFACE, type] = descriptor + maybePresent
    }

    override fun visitInterfaceRest(ctx: WebIDLParser.InterfaceRestContext) {
        val type = ctx.IDENTIFIER_WEBIDL()?.text ?: return
        context[BindingSlices.CUSTOM_TYPE, type] = type

        val superTypes = ctx.inheritance()?.IDENTIFIER_WEBIDL()?.text?.trim().let(::setOfNotNull)
        val collectedMembers = ctx.interfaceMembers()?.let { membersCollector.visit(it) }.orEmpty()

        context[BindingSlices.INTERFACE, type] = Descriptor.InterfaceDescriptor(
            name = type,
            members = collectedMembers,
            superTypes = superTypes
        )
    }

    override fun visitDictionary(ctx: WebIDLParser.DictionaryContext) {
        val name = ctx.IDENTIFIER_WEBIDL()?.text ?: return
        context[BindingSlices.CUSTOM_TYPE, name] = name
    }

    override fun visitEnum_(ctx: WebIDLParser.Enum_Context) {
        val enumName = ctx.IDENTIFIER_WEBIDL()?.text ?: return
        context[BindingSlices.CUSTOM_TYPE, enumName] = enumName
    }

    override fun visitTypedef_(ctx: WebIDLParser.Typedef_Context) {
        val type = ctx.IDENTIFIER_WEBIDL()?.text ?: return
        context[BindingSlices.CUSTOM_TYPE, type] = type
    }
}
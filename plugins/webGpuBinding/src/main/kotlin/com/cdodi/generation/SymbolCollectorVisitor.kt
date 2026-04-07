package com.cdodi.generation

import WebIDLBaseVisitor

class SymbolCollectorVisitor : WebIDLBaseVisitor<Unit>() {
    val knownEnumNames = mutableSetOf<String>()
    val typeAliases = mutableMapOf<String, String>()
    val dictionaryNodes = mutableMapOf<String, WebIDLParser.DictionaryContext>()
    val includesMap = mutableMapOf<String, MutableList<String>>()

    override fun visitIncludesStatement(ctx: WebIDLParser.IncludesStatementContext) {
        // Grab the two identifiers: [TargetClass] includes [MixinName];
        val targetClass = ctx.IDENTIFIER_WEBIDL(0)?.text ?: return
        val mixinName = ctx.IDENTIFIER_WEBIDL(1)?.text ?: return

        includesMap.getOrPut(targetClass) { mutableListOf() }.add(mixinName)
        super.visitIncludesStatement(ctx)
    }

    override fun visitDictionary(ctx: WebIDLParser.DictionaryContext) {
        val name = ctx.IDENTIFIER_WEBIDL()?.text
        if (name != null) dictionaryNodes[name] = ctx
        super.visitDictionary(ctx)
    }

    override fun visitEnum_(ctx: WebIDLParser.Enum_Context) {
        val enumName = ctx.IDENTIFIER_WEBIDL()?.text
        if (enumName != null && enumName.startsWith("GPU", ignoreCase = true)) {
            knownEnumNames.add(enumName)
        }
        // We don't need to return anything, just scrape the name!
    }

    override fun visitTypedef_(ctx: WebIDLParser.Typedef_Context) {
        val aliasName = ctx.IDENTIFIER_WEBIDL()?.text ?: return
        val typeCtx = ctx.typeWithExtendedAttributes()?.type_() ?: return

        // Check if the typedef is a union type e.g., (GPUSampler or GPUTextureView)
        if (typeCtx.unionType() != null) {
            typeAliases[aliasName] = "JsAny"
        } else {
            // It's a single type. Grab the text (e.g., "unsignedlong")
            val targetType = typeCtx.text
            typeAliases[aliasName] = targetType
        }
    }
}
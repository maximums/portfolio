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

class KotlinGenerator(
    private val knownEnumNames: Set<String>,
    private val typeAliases: Map<String, String>, // Pass the aliases here
    private val dictionaryNodes: Map<String, WebIDLParser.DictionaryContext>, // NEW
    private val includesMap: Map<String, List<String>> // NEW: Pass the map here!
) : WebIDLBaseVisitor<TypeSpec?>() {

    val generatedTypes = mutableListOf<TypeSpec>()
    val generatedFunctions = mutableListOf<FunSpec>()

    private val jsAnyClassName = ClassName("kotlin.js", "JsAny")
    private val jsArrayClassName = ClassName("kotlin.js", "JsArray")
    private val createJsObjectMember = MemberName("com.cdodi.webgpu", "createJsObject")
    private val generatedPackageName = "com.cdodi.webgpu.bindings"

    // Helper data class to store properties while we traverse the hierarchy
    private data class ParsedProperty(
        val name: String,
        val type: TypeName,
        val isRequired: Boolean,
        val isOwn: Boolean // true for child properties, false for parent properties
    )

    override fun visitMixinRest(ctx: WebIDLParser.MixinRestContext?): TypeSpec? {
        if (ctx == null) return null
        val mixinName = ctx.IDENTIFIER_WEBIDL()?.text ?: return null

        // Mixins are generated as standard external interfaces
        val interfaceBuilder = TypeSpec.interfaceBuilder(mixinName)
            .addModifiers(KModifier.EXTERNAL)
            .addSuperinterface(jsAnyClassName)

        var currentMembersCtx = ctx.mixinMembers()

        while (currentMembersCtx != null && currentMembersCtx.childCount > 0) {
            val memberCtx = currentMembersCtx.mixinMember()

            // --- 1. HANDLE ATTRIBUTES ---
            val attributeRestCtx = memberCtx?.attributeRest()
            if (attributeRestCtx != null) {
                val propertyName = attributeRestCtx.attributeName()?.text
                val typeNameText = attributeRestCtx.typeWithExtendedAttributes()?.type_()?.text

                if (propertyName != null && typeNameText != null) {
                    val kotlinType = mapWebIdlTypeToKotlin(typeNameText)
                    val isReadOnly = memberCtx.optionalReadOnly()?.text?.isNotEmpty() == true

                    interfaceBuilder.addProperty(
                        PropertySpec.builder(propertyName, kotlinType)
                            .mutable(!isReadOnly)
                            .build()
                    )
                }
            }

            // --- 2. HANDLE OPERATIONS ---
            val operationCtx = memberCtx?.regularOperation()
            if (operationCtx != null) {
                val operationName = operationCtx.operationRest()?.optionalOperationName()?.text

                if (!operationName.isNullOrEmpty()) {
                    val funBuilder = FunSpec.builder(operationName)
                        .addModifiers(KModifier.ABSTRACT)

                    val returnTypeStr = operationCtx.type_()?.text
                    // Modern WebIDL uses "undefined" instead of "void"
                    if (returnTypeStr != null && returnTypeStr != "void" && returnTypeStr != "undefined") {
                        funBuilder.returns(mapWebIdlTypeToKotlin(returnTypeStr))
                    }

                    var currentArgList = operationCtx.operationRest()?.argumentList()

                    while (currentArgList != null && currentArgList.childCount > 0) {
                        val argCtx = currentArgList.argument()
                        val argRest = argCtx?.argumentRest()
                        val argName = argRest?.argumentName()?.text

                        val argTypeStr = argRest?.typeWithExtendedAttributes()?.type_()?.text
                            ?: argRest?.type_()?.text

                        if (argName != null && argTypeStr != null) {
                            val paramType = mapWebIdlTypeToKotlin(argTypeStr)
                            val isOptional = argRest.text.startsWith("optional")
                            val finalParamType = if (isOptional) paramType.copy(nullable = true) else paramType

                            funBuilder.addParameter(argName, finalParamType)
                        }

                        currentArgList = currentArgList.arguments()?.let {
                            // Advance the grammar tree manually if we hit arguments
                            if (it.childCount > 0) it.parent as? WebIDLParser.ArgumentListContext else null
                        } ?: currentArgList.arguments()?.argument()?.parent as? WebIDLParser.ArgumentListContext
                        // *Note for safety: ANTLR's nested comma lists can be tricky here.
                        // Since your grammar structures it as: arguments -> ',' argument arguments
                        // A safer manual traversal for the arguments block:
                    }

                    // --- SAFER ARGUMENT TRAVERSAL ---
                    var argListCtx = operationCtx.operationRest()?.argumentList()
                    if (argListCtx != null && argListCtx.childCount > 0) {
                        var argCtx = argListCtx.argument()
                        var argsCtx = argListCtx.arguments()

                        while (argCtx != null) {
                            val argRest = argCtx.argumentRest()
                            val argName = argRest?.argumentName()?.text
                            val argTypeStr = argRest?.typeWithExtendedAttributes()?.type_()?.text ?: argRest?.type_()?.text

                            if (argName != null && argTypeStr != null) {
                                val paramType = mapWebIdlTypeToKotlin(argTypeStr)
                                val isOptional = argRest.text.startsWith("optional")
                                val finalParamType = if (isOptional) paramType.copy(nullable = true) else paramType
                                funBuilder.addParameter(argName, finalParamType)
                            }
                            argCtx = argsCtx?.argument()
                            argsCtx = argsCtx?.arguments()
                        }
                    }

                    interfaceBuilder.addFunction(funBuilder.build())
                }
            }
            currentMembersCtx = currentMembersCtx.mixinMembers()
        }

        val finalInterface = interfaceBuilder.build()
        generatedTypes.add(finalInterface)
        return finalInterface
    }

    override fun visitInterfaceRest(ctx: WebIDLParser.InterfaceRestContext?): TypeSpec? {
        if (ctx == null) return null

        val classNameString = ctx.IDENTIFIER_WEBIDL()?.text ?: return null

        // 1. Build an ABSTRACT EXTERNAL CLASS instead of an interface
        val classBuilder = TypeSpec.classBuilder(classNameString)
            .addModifiers(KModifier.ABSTRACT, KModifier.EXTERNAL)

        // 2. Handle Inheritance
        val parentName = ctx.inheritance()?.IDENTIFIER_WEBIDL()?.text

        // Skip ignored parents (like DOMException)
        if (parentName != null && parentName !in ignoredParents) {
            if (domTypes.containsKey(parentName)) {
                // DOM Types are open classes, use superclass!
                classBuilder.superclass(domTypes[parentName]!!)
            } else {
                // WebGPU parents are now also generated as abstract classes
                classBuilder.superclass(ClassName(generatedPackageName, parentName))
            }
        } else {
            // Implicitly extend JsAny if no parent or parent is ignored
            classBuilder.addSuperinterface(jsAnyClassName)
        }

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

                    classBuilder.addProperty(
                        PropertySpec.builder(propertyName, kotlinType)
                            .mutable(!isReadOnly) // val if readonly, var if read-write
                            .build()
                    )
                }
            }

            // --- 2. NEW: HANDLE OPERATIONS (Functions) ---
            val operationCtx = partialMemberCtx?.operation()?.regularOperation()

            if (operationCtx != null) {
                // 2a. Get Function Name
                val operationName = operationCtx.operationRest()?.optionalOperationName()?.text

                if (!operationName.isNullOrEmpty()) {
                    val funBuilder = FunSpec.builder(operationName)
                        .addModifiers(KModifier.ABSTRACT)

                    // 2b. Get Return Type
                    val returnTypeStr = operationCtx.type_()?.text
                    if (returnTypeStr != null && returnTypeStr != "void") {
                        funBuilder.returns(mapWebIdlTypeToKotlin(returnTypeStr))
                    }

                    // 2c. Traverse Arguments List
                    var currentArgList = operationCtx.operationRest()?.argumentList()

                    if (currentArgList != null && currentArgList.childCount > 0) {
                        var argCtx = currentArgList.argument()
                        var argsCtx = currentArgList.arguments()

                        while (argCtx != null) {
                            val argRest = argCtx.argumentRest()
                            val argName = argRest?.argumentName()?.text

                            // Type can be in one of two branches depending on if it's 'optional'
                            val argTypeStr = argRest?.typeWithExtendedAttributes()?.type_()?.text
                                ?: argRest?.type_()?.text

                            if (argName != null && argTypeStr != null) {
                                val paramType = mapWebIdlTypeToKotlin(argTypeStr)

                                // WebIDL uses 'optional' keyword. If present, make it nullable in Kotlin.
                                val isOptional = argRest.text.startsWith("optional")
                                val finalParamType = if (isOptional) paramType.copy(nullable = true) else paramType

                                funBuilder.addParameter(argName, finalParamType)
                            }

                            // Move to the next argument in the comma-separated list
                            argCtx = argsCtx?.argument()
                            argsCtx = argsCtx?.arguments()
                        }
                    }

                    // Add the fully constructed function to the interface
                    classBuilder.addFunction(funBuilder.build())
                }
            }

            // Move to the next member in the AST chain
            currentMembersCtx = currentMembersCtx.interfaceMembers()
        }

        val finalInterface = classBuilder.build()
        generatedTypes.add(finalInterface)

        return finalInterface
    }

    override fun visitDictionary(ctx: WebIDLParser.DictionaryContext): TypeSpec? {
        val classNameString = ctx.IDENTIFIER_WEBIDL()?.text ?: return null
        if (!classNameString.startsWith("GPU", ignoreCase = true)) return null
        val className = ClassName(generatedPackageName, classNameString)

        val interfaceBuilder = TypeSpec.interfaceBuilder(classNameString)
            .addModifiers(KModifier.EXTERNAL)

        // 1. Handle Interface Inheritance
        val parentInterfaceName = ctx.inheritance()?.IDENTIFIER_WEBIDL()?.text

        // Skip missing DOM types like EventInit
        if (parentInterfaceName != null && parentInterfaceName !in ignoredParents) {
            if (domTypes.containsKey(parentInterfaceName)) {
                interfaceBuilder.addSuperinterface(domTypes[parentInterfaceName]!!)
            } else {
                interfaceBuilder.addSuperinterface(ClassName(generatedPackageName, parentInterfaceName))
            }
        } else {
            interfaceBuilder.addSuperinterface(jsAnyClassName)
        }

        val factoryFunBuilder = FunSpec.builder(classNameString)
            .addModifiers(KModifier.INLINE)
            .returns(className)
        factoryFunBuilder.beginControlFlow("return %M<%T>", createJsObjectMember, className)

        // 2. Traverse the dictionary AND its parents to gather ALL properties
        val allProperties = mutableListOf<ParsedProperty>()
        var currentDictCtx: WebIDLParser.DictionaryContext? = ctx
        var isOwnProp = true

        while (currentDictCtx != null) {
            var currentMembersCtx = currentDictCtx.dictionaryMembers()

            while (currentMembersCtx != null && currentMembersCtx.childCount > 0) {
                val memberCtx = currentMembersCtx.dictionaryMember()
                val restCtx = memberCtx?.dictionaryMemberRest()

                if (restCtx != null) {
                    val propertyName = restCtx.IDENTIFIER_WEBIDL()?.text
                    val isRequired = restCtx.typeWithExtendedAttributes() != null
                    val typeCtx = restCtx.type_() ?: restCtx.typeWithExtendedAttributes()?.type_()

                    if (propertyName != null && typeCtx != null) {
                        val baseType = mapWebIdlTypeToKotlin(typeCtx.text)
                        val kotlinType = if (isRequired) baseType else baseType.copy(nullable = true)

                        allProperties.add(ParsedProperty(propertyName, kotlinType, isRequired, isOwnProp))
                    }
                }
                currentMembersCtx = currentMembersCtx.dictionaryMembers()
            }

            // Move UP the inheritance chain to the parent dictionary
            val nextParentName = currentDictCtx.inheritance()?.IDENTIFIER_WEBIDL()?.text
            currentDictCtx = if (nextParentName != null) dictionaryNodes[nextParentName] else null
            isOwnProp = false // Any properties we find from here on belong to the parent
        }

        // 3. Add ONLY the child's own properties to the interface
        // (Parent properties are inherited automatically by Kotlin!)
        for (prop in allProperties.filter { it.isOwn }) {
            interfaceBuilder.addProperty(
                PropertySpec.builder(prop.name, prop.type)
                    .mutable(true)
                    .build()
            )
        }

        // 4. Add ALL properties to the factory function
        // We sort them so 'required' properties come first in the constructor!
        val sortedProperties = allProperties.sortedBy { !it.isRequired }

        for (prop in sortedProperties) {
            val paramBuilder = ParameterSpec.builder(prop.name, prop.type)
            if (!prop.isRequired) paramBuilder.defaultValue("null")
            factoryFunBuilder.addParameter(paramBuilder.build())

            if (prop.isRequired) {
                factoryFunBuilder.addStatement("this.%L = %L", prop.name, prop.name)
            } else {
                factoryFunBuilder.addStatement("if (%L != null) this.%L = %L", prop.name, prop.name, prop.name)
            }
        }

        factoryFunBuilder.endControlFlow()

        val finalInterface = interfaceBuilder.build()
        generatedTypes.add(finalInterface)
        if (allProperties.isNotEmpty()) {
            generatedFunctions.add(factoryFunBuilder.build())
        }

        return finalInterface
    }

    override fun visitEnum_(ctx: WebIDLParser.Enum_Context): TypeSpec? {
        val enumName = ctx.IDENTIFIER_WEBIDL()?.text ?: return null
        if (!enumName.startsWith("GPU", ignoreCase = true)) return null

        // Create a singleton object to hold the string constants
        val objectBuilder = TypeSpec.objectBuilder(enumName)
        val enumValues = mutableListOf<String>()

        // 1. Grab the first string literal in the list
        val firstString = ctx.enumValueList()?.STRING_WEBIDL()?.text
        if (firstString != null) enumValues.add(firstString)

        // 2. Traverse the recursive right-branching comma list
        var currentCommaCtx = ctx.enumValueList()?.enumValueListComma()

        while (currentCommaCtx != null && currentCommaCtx.childCount > 0) {
            val stringCtx = currentCommaCtx.enumValueListString()
            val nextString = stringCtx?.STRING_WEBIDL()?.text

            if (nextString != null) {
                enumValues.add(nextString)
            }

            // Move to the next comma node
            currentCommaCtx = stringCtx?.enumValueListComma()
        }

        // 3. Convert the strings into Kotlin const vals
        for (enumVal in enumValues) {
            // Remove the literal quotes parsed by ANTLR (e.g., '"bgra8unorm"' -> 'bgra8unorm')
            val cleanValue = enumVal.removeSurrounding("\"")

            // Format as standard Kotlin constants (e.g., 'bgra8unorm-srgb' -> 'BGRA8UNORM_SRGB')
            val constName = cleanValue
                .uppercase()
                .replace("-", "_")
                .replace(" ", "_")

            if (constName.isNotEmpty()) {
                objectBuilder.addProperty(
                    PropertySpec.builder(constName, STRING, KModifier.CONST)
                        .initializer("%S", cleanValue) // %S safely wraps it back in quotes for the generated code
                        .build()
                )
            }
        }

        val finalObject = objectBuilder.build()
        generatedTypes.add(finalObject)

        return finalObject
    }

    private val jsStringClassName = ClassName("kotlin.js", "JsString")
    private val jsNumberClassName = ClassName("kotlin.js", "JsNumber")
    private val jsBooleanClassName = ClassName("kotlin.js", "JsBoolean")
    private val jsPromiseClassName = ClassName("kotlin.js", "Promise")

    private val domTypes = mapOf(
        "Event" to ClassName("org.w3c.dom.events", "Event"),
        "EventTarget" to ClassName("org.w3c.dom.events", "EventTarget"),
    )

    // NEW: Types missing from Kotlin/Wasm stdlib that we should ignore
    private val ignoredParents = setOf("DOMException", "EventInit")

    private fun mapWebIdlTypeToKotlin(idlType: String): TypeName {
        val isSequence = idlType.startsWith("sequence<")
        val isPromise = idlType.startsWith("Promise<") // NEW
        var cleanType = idlType
            .removePrefix("sequence<").removeSuffix(">")
            .removePrefix("Promise<").removeSuffix(">") // NEW
            .removeSuffix("?")

        // 1. Resolve Typedefs
        while (typeAliases.containsKey(cleanType)) {
            cleanType = typeAliases[cleanType]!!
        }

        // 2. Map to base Kotlin type
        val mappedType = when (cleanType) {
            "double", "float", "unrestricteddouble", "unrestrictedfloat" -> DOUBLE
            "long", "unsignedlong", "short", "unsignedshort" -> INT
            "boolean" -> BOOLEAN
            "DOMString", "USVString", "ByteString" -> STRING
            "JsAny", "any", "object" -> jsAnyClassName
            else -> {
                if (domTypes.containsKey(cleanType)) {
                    domTypes[cleanType]!!
                } else if (cleanType in ignoredParents) { // NEW: Safe fallback
                    jsAnyClassName
                } else if (cleanType in knownEnumNames) {
                    STRING
                } else if (cleanType.startsWith("GPU")) {
                    ClassName(generatedPackageName, cleanType)
                } else {
                    jsAnyClassName
                }
            }
        }

        // 3. NEW: If it's a sequence, ensure the inner type extends JsAny!
        return if (isSequence) {
            val jsCompatibleType = when (mappedType) {
                STRING -> jsStringClassName
                DOUBLE, INT -> jsNumberClassName
                BOOLEAN -> jsBooleanClassName
                else -> mappedType
            }
            jsArrayClassName.parameterizedBy(jsCompatibleType)
        } else if (isPromise) {
            // Wrap the resolved type in a JS Promise
            jsPromiseClassName.parameterizedBy(mappedType)
        } else {
            mappedType
        }
    }
}
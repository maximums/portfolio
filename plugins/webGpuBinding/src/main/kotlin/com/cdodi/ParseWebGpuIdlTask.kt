package com.cdodi

import WebIDLLexer
import WebIDLParser
import com.cdodi.transpiler.BindingSlices
import com.cdodi.transpiler.InterfaceCollector
import com.cdodi.transpiler.KotlinGenerator
import com.cdodi.transpiler.SymbolCollectorVisitor
import com.cdodi.transpiler.MutableBidingContext
import com.cdodi.transpiler.TypeResolver
import com.squareup.kotlinpoet.FileSpec
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ParseWebGpuIdlTask : DefaultTask() {

    @get:InputFile
    abstract val webGpuIdlFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    operator fun invoke() {
        val outputDir = outputDirectory.get().asFile
        val idlFile = webGpuIdlFile.get().asFile
        val lexer = WebIDLLexer(CharStreams.fromFileName(idlFile.absolutePath))
        val tokens = CommonTokenStream(lexer)
        val parser = WebIDLParser(tokens)
        val tree = parser.webIDL()
        val sematicContext = MutableBidingContext()

        val typeResolver = TypeResolver()
        val membersCollector = InterfaceCollector(typeResolver)
        val symbolCollector = SymbolCollectorVisitor(sematicContext, membersCollector).also { it.visit(tree) }
//        println(sematicContext[BindingSlices.INTERFACE])
        val generator = KotlinGenerator(
            bindingContext = sematicContext,
            typeResolver = typeResolver,
            generatedPackageName = "com.cdodi.webgpu.bindings"
        )

//        val visitor = KotlinGenerator(
//            enumCollector.knownEnumNames,
//            enumCollector.typeAliases,
//            enumCollector.dictionaryNodes,
//            enumCollector.includesMap,
//        )
//        tree.accept(visitor)

        outputDir.mkdirs()
        outputDir.deleteRecursively()
//        val factoriesFile = File(outputDir, "WebGpuFactories.kt")
//        val bindingFile = File(outputDir, "WebGpuBindings.kt")

        val enums = tree.definitions().definition()
        val fileBuilder = FileSpec.builder("com.cdodi.webgpu.bindings", "WebGpuBindings")
        val spec = generator.visit(enums)
        spec?.let { fileBuilder.addType(it) }
//        val factoriesFileBuilder = FileSpec.builder("com.cdodi.webgpu.bindings", "WebGpuFactories")

//        visitor.generatedTypes.forEach { typeSpec -> fileBuilder.addType(typeSpec) }
//        visitor.generatedFunctions.forEach { functionSpec -> factoriesFileBuilder.addFunction(functionSpec) }

        fileBuilder.build().writeTo(outputDir)
//        factoriesFileBuilder.build().writeTo(outputDir)
    }
}





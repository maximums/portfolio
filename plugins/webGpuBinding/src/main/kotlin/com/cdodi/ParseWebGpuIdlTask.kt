package com.cdodi

import WebIDLLexer
import WebIDLParser
import com.cdodi.generation.SymbolCollectorVisitor
import com.cdodi.generation.KotlinGenerator
import com.squareup.kotlinpoet.FileSpec
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

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

        val enumCollector = SymbolCollectorVisitor()
        enumCollector.visit(tree)
        val visitor = KotlinGenerator(
            enumCollector.knownEnumNames,
            enumCollector.typeAliases,
            enumCollector.dictionaryNodes,
            enumCollector.includesMap,
        )
        tree.accept(visitor)

        outputDir.mkdirs()
        outputDir.deleteRecursively()
//        val factoriesFile = File(outputDir, "WebGpuFactories.kt")
//        val bindingFile = File(outputDir, "WebGpuBindings.kt")

        val fileBuilder = FileSpec.builder("com.cdodi.webgpu.bindings", "WebGpuBindings")
        val factoriesFileBuilder = FileSpec.builder("com.cdodi.webgpu.bindings", "WebGpuFactories")

        visitor.generatedTypes.forEach { typeSpec -> fileBuilder.addType(typeSpec) }
        visitor.generatedFunctions.forEach { functionSpec -> factoriesFileBuilder.addFunction(functionSpec) }

        fileBuilder.build().writeTo(outputDir)
        factoriesFileBuilder.build().writeTo(outputDir)
    }
}





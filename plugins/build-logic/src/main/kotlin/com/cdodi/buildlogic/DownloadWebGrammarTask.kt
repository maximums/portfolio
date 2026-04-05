package com.cdodi.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@CacheableTask
abstract class DownloadWebGrammarTask : DefaultTask() {

    @get:Input
    abstract val grammarUrl: Property<String>

    @get:OutputFile
    abstract val grammarFile: RegularFileProperty

    @TaskAction
    operator fun invoke() {
        val stringUrl = grammarUrl.get()
        val uri = URI.create(stringUrl)
        val file = grammarFile.get().asFile

        logger.lifecycle("Grammar task started: $stringUrl")

        file.parentFile.mkdirs()

        uri.toURL().openStream().use { input ->
            Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
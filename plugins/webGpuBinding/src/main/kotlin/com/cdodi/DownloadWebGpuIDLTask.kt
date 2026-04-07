package com.cdodi

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
abstract class DownloadWebGpuIDLTask : DefaultTask() {

    @get:Input
    abstract val webGpuIdlUrl: Property<String>

    @get:OutputFile
    abstract val webGpuIdlFile: RegularFileProperty

    @TaskAction
    operator fun invoke() {
        val stringUrl = webGpuIdlUrl.get()
        val uri = URI.create(stringUrl)
        val file = webGpuIdlFile.get().asFile

        logger.lifecycle("WebGPU IDL task started: $stringUrl")

        file.parentFile.mkdirs()

        uri.toURL().openStream().use { input ->
            Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

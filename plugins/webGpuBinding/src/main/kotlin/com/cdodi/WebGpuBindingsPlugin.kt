@file:Suppress("unused")

package com.cdodi

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class WebGpuBindingsPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val downloadTask = tasks.register<DownloadWebGpuIDLTask>("downloadWebGpuIdl") {
            group = TASKS_GROUP
            webGpuIdlUrl = providers.gradleProperty("webGpuIdlUrl")
            webGpuIdlFile = layout.projectDirectory.file("webgpu/webgpu.idl")
        }

        val parserTask = tasks.register<ParseWebGpuIdlTask>("parseWebGpuIdl") {
            group = TASKS_GROUP
            webGpuIdlFile = downloadTask.flatMap { it.webGpuIdlFile }
            outputDirectory = layout.buildDirectory.dir("webgpu-src")
        }

        logger.info("WebGpuBindings plugin applied.")
    }

    private companion object {
        const val TASKS_GROUP = "webgpu"
    }
}

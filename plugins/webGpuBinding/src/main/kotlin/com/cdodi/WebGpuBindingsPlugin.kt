@file:Suppress("unused")

package com.cdodi

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class WebGpuBindingsPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val apiTask = tasks.register<DownloadWebGpuIDLTask>("downloadWebGpuIdl") {
            group = TASKS_GROUP
            webGpuIdlUrl = providers.gradleProperty("webGpuIdlUrl")
            webGpuIdlFile = layout.projectDirectory.file("webgpu/webgpu.idl")
        }

//        val lexer = WebIDLLexer()

        logger.info("WebGpuBindings plugin applied.")
    }

    private companion object {
        const val TASKS_GROUP = "webgpu"
    }
}

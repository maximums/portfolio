import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.cdodi.webgpu.bindings")
}

kotlin {

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static(rootDirPath)
                }
            }
        }
        binaries.executable()
    }

    compilerOptions {
        optIn = listOf(
            "androidx.compose.ui.ExperimentalComposeUiApi",
            "androidx.compose.animation.core.ExperimentalAnimatableApi",
            "androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "org.jetbrains.compose.resources.ExperimentalResourceApi",
            "kotlin.js.ExperimentalWasmJsInterop",
        )
    }

    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}

package com.cdodi.webgpu

import com.cdodi.webgpu.canvas.GPUCanvasConfiguration
import com.cdodi.webgpu.canvas.createJsObject
import com.cdodi.webgpu.canvas.getCanvasContext
import com.cdodi.webgpu.canvas.prepareCanvasConfig
import com.cdodi.webgpu.command.prepareRenderPassDescriptor
import com.cdodi.webgpu.core.Device
import com.cdodi.webgpu.core.GPUAdapter
import com.cdodi.webgpu.core.gpu
import com.cdodi.webgpu.pipeline.fragmentState
import com.cdodi.webgpu.pipeline.prepareShaderModuleDescriptor
import com.cdodi.webgpu.pipeline.renderPipelineDescriptor
import com.cdodi.webgpu.pipeline.vertexState
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLCanvasElement

suspend fun prepareWebGPUCanvas() {
    val canvas = document.getElementById("webgpu-canvas") as HTMLCanvasElement
    val context = getCanvasContext(canvas)

    val gpu = gpu() ?: return
    val adapter = gpu.requestAdapter().await<GPUAdapter>()
    val device = adapter.requestDevice().await<Device>()
    val canvasFormat = gpu.getPreferredCanvasFormat()
    val temp = createJsObject<GPUCanvasConfiguration>()
    val config = prepareCanvasConfig(device, canvasFormat)
    context.configure(config)

    val shaderModuleDescriptor = prepareShaderModuleDescriptor(
        label = "our hardcoded red triangle shaders",
        code = myShader
    )
    val shaderModule = device.createShaderModule(shaderModuleDescriptor)
    val vertex = vertexState(shaderModule, "vs")
    val fragment = fragmentState(shaderModule, "fs", canvasFormat)
    val pipelineDescriptor = renderPipelineDescriptor(
        label = "our hardcoded red triangle pipeline",
        layout = "auto".toJsString(),
        vertex = vertex,
        fragment = fragment
    )
    val pipeline = device.createRenderPipeline(pipelineDescriptor)

    val descriptor = prepareRenderPassDescriptor(context)
    val encoder = device.createCommandEncoder()
    val pass = encoder.beginRenderPass(descriptor)
    pass.setPipeline(pipeline)
    pass.draw(3)
    pass.end()

    val buffer = encoder.finish()
    device.queue.submit(arrayOf(buffer).toJsArray())

    println("Got something:\n$temp")
}

// language=WGSL
private const val myShader = """
@vertex fn vs(@builtin(vertex_index) vertexIndex : u32) -> @builtin(position) vec4f {
    let pos = array(
      vec2f( 0.0,  0.5),  // top center
      vec2f(-0.5, -0.5),  // bottom left
      vec2f( 0.5, -0.5)   // bottom right
    );

    return vec4f(pos[vertexIndex], 0.0, 1.0);
}
 
@fragment fn fs() -> @location(0) vec4f {
    return vec4f(1.0, 0.0, 0.0, 1.0);
}
"""
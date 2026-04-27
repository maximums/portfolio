package com.cdodi.webgpu

//import com.cdodi.webgpu.canvas.GPUCanvasConfiguration
//import com.cdodi.webgpu.canvas.createJsObject
//import com.cdodi.webgpu.canvas.getCanvasContext
//import com.cdodi.webgpu.canvas.prepareCanvasConfig
//import com.cdodi.webgpu.command.prepareRenderPassDescriptor
//import com.cdodi.webgpu.core.Device
//import com.cdodi.webgpu.core.GPUAdapter
//import com.cdodi.webgpu.core.gpu
//import com.cdodi.webgpu.pipeline.fragmentState
//import com.cdodi.webgpu.pipeline.prepareShaderModuleDescriptor
//import com.cdodi.webgpu.pipeline.renderPipelineDescriptor
//import com.cdodi.webgpu.pipeline.vertexState
import com.cdodi.webgpu.bindings.GPUAutoLayoutMode
import com.cdodi.webgpu.bindings.GPUCanvasAlphaMode
import com.cdodi.webgpu.bindings.GPUCanvasAlphaModeEntries
import com.cdodi.webgpu.bindings.GPUCanvasToneMapping
import com.cdodi.webgpu.bindings.GPUCanvasToneMappingModeEntries
import com.cdodi.webgpu.bindings.GPUColorTargetState
import com.cdodi.webgpu.bindings.GPUDeviceDescriptor
import com.cdodi.webgpu.bindings.GPUFragmentState
import com.cdodi.webgpu.bindings.GPULoadOp
import com.cdodi.webgpu.bindings.GPURenderPassColorAttachment
import com.cdodi.webgpu.bindings.GPURenderPassDescriptor
import com.cdodi.webgpu.bindings.GPURenderPipelineDescriptor
import com.cdodi.webgpu.bindings.GPURequestAdapterOptions
import com.cdodi.webgpu.bindings.GPUShaderModuleDescriptor
import com.cdodi.webgpu.bindings.GPUStoreOp
import com.cdodi.webgpu.bindings.GPUTextureFormat
import com.cdodi.webgpu.bindings.GPUTextureFormatEntries
import com.cdodi.webgpu.bindings.GPUVertexState
import com.cdodi.webgpu.bindings.GPUCanvasContext
import com.cdodi.webgpu.bindings.GPUAdapter
import com.cdodi.webgpu.bindings.GPUDevice
import com.cdodi.webgpu.bindings.GPUCanvasConfiguration
import com.cdodi.webgpu.core.gpu
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.HTMLCanvasElement

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
suspend fun prepareWebGPUCanvas() {
    val canvas = document.getElementById("webgpu-canvas") as? HTMLCanvasElement
    val context = canvas?.getContext("webgpu") as? GPUCanvasContext
    val gpu = gpu() ?: return
    val adapter = gpu.requestAdapter().await<GPUAdapter>()
    val device2 = adapter.requestDevice().await<GPUDevice>()
    val canvasFormat = gpu.getPreferredCanvasFormat()
    val config = createJsObject<GPUCanvasConfiguration> {
        device = device2
        format = canvasFormat
    }
    context?.configure(config)

//    val shaderModuleDescriptor = GPUShaderModuleDescriptor(code = myShader)
//    val shaderModule = device.createShaderModule(shaderModuleDescriptor)
//    val vertex = GPUVertexState(shaderModule )
//    val colorTarget = GPUColorTargetState(
//        format = canvasFormat,
//        blend = null,
//        writeMask = null
//    )
//    val fragment = GPUFragmentState(
//        targets = arrayOf(colorTarget).toJsArray(),
//        module = shaderModule
//    )
//    val pipelineDescriptor = GPURenderPipelineDescriptor(
//        vertex = vertex,
//        layout = GPUAutoLayoutMode.AUTO.toJsString(),
//        fragment = fragment,
//    )
//    val pipeline = device.createRenderPipeline(pipelineDescriptor)
//    val renderPassColorAttachments = arrayOf(
//        GPURenderPassColorAttachment(
//            view = context.getCurrentTexture().createView(null),
//            loadOp = GPULoadOp.CLEAR,
//            storeOp = GPUStoreOp.STORE
//        )
//    )
//    val descriptor = GPURenderPassDescriptor(renderPassColorAttachments.toJsArray())
//    val encoder = device.createCommandEncoder(null)
//    val pass = encoder.beginRenderPass(descriptor)
//    pass.setPipeline(pipeline)
//    pass.draw(3)
//    pass.end()
//
//    val buffer = encoder.finish()
//    device.queue.submit(arrayOf(buffer).toJsArray())

    println("Got something:")
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
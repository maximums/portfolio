package com.cdodi.webgpu.core

import com.cdodi.webgpu.bindings.GPU

//import com.cdodi.webgpu.canvas.GPUTextureFormat
//import com.cdodi.webgpu.command.GPUCommandEncoder
//import com.cdodi.webgpu.pipeline.GPURenderPipeline
//import com.cdodi.webgpu.pipeline.GPURenderPipelineDescriptor
//import com.cdodi.webgpu.pipeline.GPUShaderModule
//import com.cdodi.webgpu.pipeline.GPUShaderModuleDescriptor
//import kotlin.js.Promise
//
//external interface GPUQueue : JsAny {
//    fun submit(cmds: JsArray<JsAny>)
//}
//external interface Device : JsAny {
//    val queue: GPUQueue
//    fun createCommandEncoder(): GPUCommandEncoder
//
//    fun createShaderModule(descriptor: GPUShaderModuleDescriptor): GPUShaderModule
//
//    fun createRenderPipeline(descriptor: GPURenderPipelineDescriptor): GPURenderPipeline
//}
//
//external interface GPUAdapter : JsAny {
//    fun requestDevice(): Promise<Device>
//}
//
//external interface WebGpu : JsAny {
//
//    // without any options for now
//    fun requestAdapter(): Promise<GPUAdapter?>
//
//    fun getPreferredCanvasFormat(): GPUTextureFormat
//}
//
@JsFun("() => navigator.gpu")
external fun gpu(): GPU?
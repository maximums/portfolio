package com.cdodi.webgpu.pipeline

import com.cdodi.webgpu.canvas.GPUTextureFormat

external interface GPURenderPipeline : JsAny

external interface GPUShaderModuleDescriptor : JsAny

external interface GPUShaderModule : JsAny

external interface GPUVertexState : JsAny {
    val module: GPUShaderModule
    val entryPoint: String
}

external interface GPUFragmentState : JsAny {
    var module: GPUShaderModule
    var entryPoint: String
    var targets: JsArray<JsAny>
}

external interface GPURenderPipelineDescriptor : JsAny {
    val label: String?
    val layout: JsAny
    val vertex: GPUVertexState
    val fragment: GPUFragmentState?
}

@JsFun("(label, code) => ({ label: label, code: code })")
external fun prepareShaderModuleDescriptor(label: String, code: String): GPUShaderModuleDescriptor

@JsFun("(module, entry) => ({ module: module, entryPoint: entry })")
external fun vertexState(
    module: GPUShaderModule,
    entry: String
): GPUVertexState

@JsFun("(module, entry, format) => ({ module: module, entryPoint: entry, targets: [{ format: format }] })")
external fun fragmentState(
    module: GPUShaderModule,
    entry: String,
    format: GPUTextureFormat
): GPUFragmentState

@JsFun("(label, layout, vertex, fragment) => ({ label, layout, vertex, fragment })")
external fun renderPipelineDescriptor(
    label: String?,
    layout: JsAny,
    vertex: GPUVertexState,
    fragment: GPUFragmentState?
): GPURenderPipelineDescriptor
package com.cdodi.webgpu.command
//
//import com.cdodi.webgpu.canvas.GPUCanvasContext
//import com.cdodi.webgpu.pipeline.GPURenderPipeline
//
//external interface GPURenderPassEncoder : JsAny {
//    fun setPipeline(pipeline: GPURenderPipeline)
//    fun draw(vertexCount: Int)
//    fun end()
//}
//external interface GPURenderPassDescriptor : JsAny
//external interface GPUCommandEncoder : JsAny {
//    fun beginRenderPass(descriptor: GPURenderPassDescriptor): GPURenderPassEncoder
//    fun finish(): JsAny
//}
//
//// hardcode for now
//@JsFun("""(context) => ({
//    label: 'our basic canvas renderPass',
//    colorAttachments: [
//    {  view: context.getCurrentTexture().createView(),
//       loadOp: 'clear',
//       clearValue: { r: 0, g: 0, b: 0.4, a: 1 },
//       storeOp: 'store'
//    }
//    ]})""")
//external fun prepareRenderPassDescriptor(context: GPUCanvasContext): GPURenderPassDescriptor
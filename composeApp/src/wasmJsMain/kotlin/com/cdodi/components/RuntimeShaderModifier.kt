package com.cdodi.components

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.util.fastRoundToInt
import com.cdodi.uniformData
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RuntimeEffect

private const val ONE_SECOND_NANOS = 1_000_000_000f

@Immutable
value class RuntimeShader(val value: String)

class RuntimeShaderModifierNode(
    private var shader: RuntimeShader,
): DrawModifierNode, Modifier.Node() {

    private lateinit var runtimeEffect: RuntimeEffect
    private val cachedPaint = Paint()
    private var time: Float = 0f

    fun updateShader(newShader: RuntimeShader) {
        if (shader == newShader) return

        shader = newShader
        runtimeEffect = RuntimeEffect.makeForShader(shader.value)
        invalidateDraw()
    }

    override fun onAttach() {
        super.onAttach()

        runtimeEffect = RuntimeEffect.makeForShader(shader.value)

        coroutineScope.launch(CoroutineName("ShaderBackground")) {
            val startTime = withFrameNanos { it }
            while (isActive) {
                withInfiniteAnimationFrameNanos { frameTimeNanos ->
                    time = (frameTimeNanos - startTime) / ONE_SECOND_NANOS
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        cachedPaint.shader = runtimeEffect.makeShader(
            uniforms = uniformData(
                size.width.fastRoundToInt(),
                size.height.fastRoundToInt(),
                time
            ),
            children = null,
            localMatrix = null,
        )

        drawContext.canvas.nativeCanvas.drawPaint(cachedPaint)
        drawContent()
    }
}

data class RuntimeShaderModifierNodeElement(
    private val shader: RuntimeShader,
): ModifierNodeElement<RuntimeShaderModifierNode>() {
    override fun create() = RuntimeShaderModifierNode(shader)

    override fun update(node: RuntimeShaderModifierNode) = node.updateShader(newShader = shader)
}

fun Modifier.backgroundShader(shader: RuntimeShader) = this then(RuntimeShaderModifierNodeElement(shader))
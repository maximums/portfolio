package com.cdodi.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import kotlinx.coroutines.launch

class MorphingShapeModifierNode(
    initialShape: QuadVertexProgress,
) : DrawModifierNode, Modifier.Node() {

    private val cachedPath = Path()
    private val morphingAnimation: Animatable<QuadVertexProgress, AnimationVector4D> = Animatable(
        initialValue = initialShape,
        typeConverter = VertexesProgressToVector,
        label = "MorphingShapeAnimation",
    )

    fun updateTargetShape(newShape: QuadVertexProgress, durationMillis: Int, easing: Easing) {
        if (morphingAnimation.targetValue != newShape) {
            coroutineScope.launch {
                morphingAnimation.animateTo(
                    targetValue = newShape,
                    animationSpec = tween(durationMillis = durationMillis, easing = easing)
                )
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val progress = morphingAnimation.value
        val width = size.width
        val height = size.height

        with(cachedPath) {
            rewind()
            moveTo(progress.topStart * width, 0f)
            lineTo(progress.topEnd * width, 0f)
            lineTo(progress.bottomEnd * width, height)
            lineTo(progress.bottomStart * width, height)
            close()
        }

        clipPath(cachedPath) {
            this@draw.drawContent()
        }
    }
}

data class MorphingShapeModifierNodeElement(
    val targetShape: MorphingShape,
    val durationMillis: Int,
    val easing: Easing,
): ModifierNodeElement<MorphingShapeModifierNode>() {
    override fun create() = MorphingShapeModifierNode(targetShape.toVertexesProgress())

    override fun update(node: MorphingShapeModifierNode) = node.updateTargetShape(
        newShape = targetShape.toVertexesProgress(),
        durationMillis = durationMillis,
        easing = easing
    )
}

fun Modifier.morphingShape(
    targetShape: MorphingShape,
    durationMillis: Int = 1500,
    easing: Easing = FastOutSlowInEasing,
): Modifier = this then MorphingShapeModifierNodeElement(targetShape, durationMillis, easing)
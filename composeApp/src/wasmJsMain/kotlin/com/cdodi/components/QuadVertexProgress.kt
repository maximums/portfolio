package com.cdodi.components

import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter

data class QuadVertexProgress(
    val topStart: Float,
    val topEnd: Float,
    val bottomEnd: Float,
    val bottomStart: Float,
)

enum class MorphingShape(
    val topStart: Float,
    val topEnd: Float,
    val bottomEnd: Float,
    val bottomStart: Float,
) {
    Rectangle(0f, 1f, 1f, 0f),
    TriangleTopStart(0f, 1f, 0f, 0f),
    TriangleTopEnd(0f, 1f, 1f, 1f),
    TriangleBottomStart(0f, 0f, 1f, 0f),
    TriangleBottomEnd(1f, 1f, 1f, 0f);

    fun toVertexesProgress() = QuadVertexProgress(topStart, topEnd, bottomEnd, bottomStart)
}

val VertexesProgressToVector: TwoWayConverter<QuadVertexProgress, AnimationVector4D> =
    TwoWayConverter(
        convertToVector = { shape ->
            AnimationVector4D(
                v1 = shape.topStart,
                v2 = shape.topEnd,
                v3 = shape.bottomEnd,
                v4 = shape.bottomStart
            )
        },
        convertFromVector = { vector ->
            QuadVertexProgress(
                topStart = vector.v1,
                topEnd = vector.v2,
                bottomEnd = vector.v3,
                bottomStart = vector.v4
            )
        }
    )

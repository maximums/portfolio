package com.cdodi.components

import androidx.compose.animation.core.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ApproachLayoutModifierNode
import androidx.compose.ui.layout.ApproachMeasureScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round

// 1. lookaheadSize - final size of component
// 2. lookaheadCoordinates - final position of component
// 3. lookaheadScopeCoordinates - boundaries of LookaheadScope
// 4. coordinates - current position of component
// 5. localLookaheadPositionOf - `Future` layout map
// 6. localPositionOf - `Present` layout map

class PlacementModifierNode(
    var lookaheadScope: LookaheadScope,
    var durationMillis: Int,
    var easing: Easing,
) : ApproachLayoutModifierNode, Modifier.Node() {
    private val offsetAnimation: DeferredTargetAnimation<IntOffset, AnimationVector2D> =
        DeferredTargetAnimation(IntOffset.VectorConverter)
    private val sizeAnimation: DeferredTargetAnimation<IntSize, AnimationVector2D> =
        DeferredTargetAnimation(IntSize.VectorConverter)

    private val offsetAnimSpec: FiniteAnimationSpec<IntOffset> = tween(durationMillis = durationMillis, easing = easing)
    private val sizeAnimSpec: FiniteAnimationSpec<IntSize> = tween(durationMillis = durationMillis, easing = easing)

    override fun isMeasurementApproachInProgress(lookaheadSize: IntSize): Boolean {
        sizeAnimation.updateTarget(lookaheadSize, coroutineScope, sizeAnimSpec)

        return !sizeAnimation.isIdle
    }

    override fun Placeable.PlacementScope.isPlacementApproachInProgress(lookaheadCoordinates: LayoutCoordinates): Boolean {
        val target = with(lookaheadScope) {
            lookaheadScopeCoordinates
                .localLookaheadPositionOf(lookaheadCoordinates)
                .round()
        }

        offsetAnimation.updateTarget(target, coroutineScope, offsetAnimSpec)

        return !offsetAnimation.isIdle
    }

    override fun ApproachMeasureScope.approachMeasure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val (animWidth, animHeight) = sizeAnimation.updateTarget(lookaheadSize, coroutineScope, sizeAnimSpec)
        val placeable = measurable.measure(Constraints.fixed(animWidth, animHeight))

        return layout(placeable.width, placeable.height) {
            val finalOffset = coordinates?.let { currentCoordinates ->
                val target = with(lookaheadScope) {
                    lookaheadScopeCoordinates
                        .localLookaheadPositionOf(currentCoordinates)
                        .round()
                }

                val animatedOffset = offsetAnimation.updateTarget(target, coroutineScope, offsetAnimSpec)
                val placementOffset = with(lookaheadScope) {
                    lookaheadScopeCoordinates
                        .localPositionOf(currentCoordinates, Offset.Zero)
                        .round()
                }

                animatedOffset - placementOffset
            } ?: IntOffset.Zero

            placeable.place(finalOffset)
        }
    }
}

data class PlacementNodeElement(
    val lookaheadScope: LookaheadScope,
    val durationMillis: Int,
    val easing: Easing,
) : ModifierNodeElement<PlacementModifierNode>() {

    override fun update(node: PlacementModifierNode) {
        node.lookaheadScope = lookaheadScope
        node.durationMillis = durationMillis
        node.easing = easing
    }

    override fun create() = PlacementModifierNode(lookaheadScope, durationMillis, easing)
}

fun Modifier.animatePlacement(
    lookaheadScope: LookaheadScope,
    durationMillis: Int = 1500,
    easing: Easing = FastOutSlowInEasing,
): Modifier = then(PlacementNodeElement(lookaheadScope, durationMillis, easing))

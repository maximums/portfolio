package com.cdodi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

@Composable
fun UiCard(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    orientation: Orientation,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {

    val shape = when (orientation) {
        Orientation.TopStart -> AbsoluteCutCornerShape(topRightPercent = 50, bottomLeftPercent = 50)
        Orientation.TopEnd -> AbsoluteCutCornerShape(topLeftPercent = 50, bottomRightPercent = 50)
        Orientation.BottomStart -> AbsoluteCutCornerShape(topLeftPercent = 50, bottomRightPercent = 50)
        Orientation.BootomEnd -> AbsoluteCutCornerShape(topRightPercent = 50, bottomLeftPercent = 50)
    }

    Box(
        contentAlignment = contentAlignment,
        content = content,
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = Color(0xFF_5a_d6_ff),
                shape = shape
            )
            .background(color = Color(0xA0_00_00_00))
            .clickable(enabled = true, onClick = onClick) // TODO finish later with LocalIndications
    )
}

enum class Orientation {
    TopStart,
    TopEnd,
    BottomStart,
    BootomEnd;
}
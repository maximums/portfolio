package com.cdodi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

@Composable
fun UiCard(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        contentAlignment = contentAlignment,
        content = content,
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color(0xFF_5a_d6_ff),
            )
            .background(color = Color(0xA0_00_00_00))
            .clickable(enabled = true, onClick = onClick) // TODO finish later with LocalIndications
    )
}
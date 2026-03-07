package com.cdodi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cdodi.vw

val topBarModifier = Modifier.size(300.dp, 100.dp)

@Composable
fun movableCard(
    text: String,
    onClick: () -> Unit,
): @Composable LookaheadScope.(Modifier, MorphingShape) -> Unit {

    val interactionSource = remember { MutableInteractionSource() }
    val currentOnClick by rememberUpdatedState(onClick)

    return remember {
        movableContentWithReceiverOf { modifier, targetShape ->
            Box(
                contentAlignment = targetShape.contentAlignment,
                modifier = modifier
                    .animatePlacement(lookaheadScope = this)
                    .morphingShape(targetShape)
                    .background(color = Color(0xA0_00_00_00))
//                    .background(color = Color.White)
                    .clickable(onClick = currentOnClick)
                    .padding(8.dp)
            ) {
                Text(
                    modifier = Modifier.animatePlacement(this@movableContentWithReceiverOf),
                    text = text,
                    fontSize = 24.sp,
                    color = Color(0xa0_5a_d6_ff),
                )
            }
        }
    }
}

@Composable
fun movableBodyCard(): @Composable LookaheadScope.(Modifier, MorphingShape, @Composable () -> Unit) -> Unit {
    return remember {
        movableContentWithReceiverOf { modifier, targetShape, content ->
            Box(
                contentAlignment = targetShape.contentAlignment,
                modifier = modifier
                    .animatePlacement(lookaheadScope = this)
                    .morphingShape(targetShape)
                    .background(color = Color(0xA0_00_00_00))
                    .padding(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun MainMenu(
    body: @Composable BoxScope.() -> Unit,
    menuContent: @Composable BoxScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(30.vw)
    ) {
//        FlowRow(
//            modifier = Modifier.fillMaxSize(),
//            maxItemsInEachRow = 2,
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
           menuContent()
//        }

        body()
    }
}

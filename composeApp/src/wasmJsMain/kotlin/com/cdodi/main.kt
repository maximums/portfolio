package com.cdodi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.window.ComposeViewport
import com.cdodi.components.*
import com.cdodi.pages.GameOfLifePage
import kotlinx.browser.document
import org.jetbrains.skia.*
import org.jetbrains.skia.Paint

sealed class Screen {
    data object Home : Screen()
    data object About : Screen()
    data object Contacts : Screen()
    data object GameOfLife : Screen()
}

private val topBarModifier = Modifier.size(300.dp, 100.dp)
private val homeModifier = Modifier.size(200.dp, 250.dp)
private val aboutModifier = Modifier.size(200.dp, 250.dp)
private val contactsModifier = Modifier.size(200.dp, 250.dp)
private val sketchModifier = Modifier.size(200.dp, 250.dp)

fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}

@Composable
private fun App() {
    val time by iTime
    val shaderSource by rememberShader("bokeh")

    MaterialTheme {
        AppContent(shaderSource, time)
    }
}

@Composable
private fun AppContent(shaderSource: String, time: Float) {
    val runtimeEffect = remember(shaderSource) { RuntimeEffect.makeForShader(shaderSource) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val homeButton = movableButton(
        text = "Home",
        onClick = { currentScreen = Screen.Home}
    )

    val aboutButton = movableButton(
        text = "About",
        onClick = { currentScreen = Screen.About }
    )

    val contactsButton = movableButton(
        text = "Contacts",
        onClick = { currentScreen = Screen.Contacts }
    )

    val sketchButton = movableButton(
        text = "Game Of Life",
        onClick = { currentScreen = Screen.GameOfLife }
    )

    LookaheadScope {
            BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .drawWithCache {
                        val shaderPaint = Paint().apply {
                            shader = runtimeEffect.makeShader(
                                uniforms = uniformData(
                                    size.width.fastRoundToInt(),
                                    size.height.fastRoundToInt(),
                                    time
                                ),
                                children = null,
                                localMatrix = null
                            )
                        }

                        onDrawBehind {
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawPaint(shaderPaint)
                            }
                        }
                    }
            ) {
                if (currentScreen == Screen.Home) {
                    CenterMenuForm {
                        homeButton(homeModifier, MorphingShape.TriangleTopStart)
                        aboutButton(aboutModifier, MorphingShape.TriangleTopEnd)
                        contactsButton(contactsModifier, MorphingShape.TriangleBottomStart)
                        sketchButton(sketchModifier, MorphingShape.TriangleBottomEnd)
                    }
                } else {
                    TopBarForm {
                        homeButton(topBarModifier, MorphingShape.Rectangle)
                        aboutButton(topBarModifier, MorphingShape.Rectangle)
                        contactsButton(topBarModifier, MorphingShape.Rectangle)
                        sketchButton(topBarModifier, MorphingShape.Rectangle)
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 120.dp)
                    ) {
                        when (currentScreen) {
                            Screen.About -> AboutPage()
                            Screen.Contacts -> ContactsPage()
                            Screen.GameOfLife -> GameOfLifePage()
                            Screen.Home -> Unit
                        }
                    }
                }
            }
        }
    }

@Composable
fun movableButton(
    text: String,
    onClick: () -> Unit,
): @Composable LookaheadScope.(Modifier, MorphingShape) -> Unit {
    return remember {
        movableContentWithReceiverOf { modifier, targetShape ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .animatePlacement(lookaheadScope = this)
                    .morphingShape(targetShape)
                    .background(color = Color(0xA0_00_00_00))
//                    .border(width = 1.dp, color = Color(0xFF_5a_d6_ff))
                    .clickable(interactionSource = null, onClick = onClick, indication = null)
            ) {
                Text(text = text, fontSize = 30.sp, color = Color(0xa0_5a_d6_ff))
            }
        }
    }
}


@Composable
private fun CenterMenuForm(content: @Composable () -> Unit) {
    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content()
    }
}

@Composable
private fun BoxScope.TopBarForm(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.align(Alignment.TopCenter),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content()
    }
}

@Composable
private fun AboutPage() {
    Text("About Page", fontSize = 24.sp, color = Color.White)
}

@Composable
private fun ContactsPage() {
    Text("Contacts Page", fontSize = 24.sp, color = Color.White)
}

@Composable
private fun TestMorphing() {
    var state by remember { mutableStateOf(true) }
    var shape by remember(state) { mutableStateOf(if (state) MorphingShape.TriangleTopStart else MorphingShape.TriangleBottomEnd) }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 3. The Morphing Box
        Box(
            modifier = Modifier
                .size(250.dp)
                // Apply your custom modifier!
                .morphingShape(targetShape = shape)
                // The background color will automatically be clipped by the path in your modifier
                .background(Color(0xFF6200EA))
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 4. The Trigger
        Button(onClick = { state = !state }) {
            Text(text = "Morph to Next Shape")
        }
    }
}

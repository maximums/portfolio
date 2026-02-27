package com.cdodi

import androidx.compose.foundation.layout.*
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
import kotlinx.browser.document
import org.jetbrains.skia.*
import org.jetbrains.skia.Paint

sealed class Screen {
    data object Home : Screen()
    data object About : Screen()
    data object Contacts : Screen()
    data object Sketch : Screen()
}

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
                val homeButton = movableButton(
                    text = "Home",
                    orientation = Orientation.TopStart,
                    onClick = { currentScreen = Screen.Home }
                )

                val aboutButton = movableButton(
                    text = "About",
                    orientation = Orientation.TopEnd,
                    onClick = { currentScreen = Screen.About }
                )

                val contactsButton = movableButton(
                    text = "Contacts",
                    orientation = Orientation.BottomStart,
                    onClick = { currentScreen = Screen.Contacts }
                )

                val sketchButton = movableButton(
                    text = "Sketch",
                    orientation = Orientation.BootomEnd,
                    onClick = { currentScreen = Screen.Sketch }
                )

                if (currentScreen == Screen.Home) {
                    CenterMenuForm {
                        homeButton(Modifier.size(200.dp, 250.dp))
                        aboutButton(Modifier.size(200.dp, 250.dp))
                        contactsButton(Modifier.size(200.dp, 250.dp))
                        sketchButton(Modifier.size(200.dp, 250.dp))
                    }
                } else {
                    TopBarForm {
                        homeButton(Modifier.size(300.dp, 100.dp))
                        aboutButton(Modifier.size(300.dp, 100.dp))
                        contactsButton(Modifier.size(300.dp, 100.dp))
                        sketchButton(Modifier.size(300.dp, 100.dp))
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 120.dp)
                    ) {
                        when (currentScreen) {
                            Screen.About -> AboutPage()
                            Screen.Contacts -> ContactsPage()
                            Screen.Sketch -> SketchPage()
                            Screen.Home -> {}
                        }
                    }
                }
            }
        }
    }

@Composable
fun movableButton(
    text: String,
    orientation: Orientation,
    onClick: () -> Unit = {},
): @Composable LookaheadScope.(Modifier) -> Unit {
    return remember {
        movableContentWithReceiverOf { modifier ->
            UiCard(
                orientation = orientation,
                onClick = onClick,
                modifier = modifier.then(AnimatePlacementNodeElement(lookaheadScope = this))
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
private fun SketchPage() {
    Text("Sketch Page", fontSize = 24.sp, color = Color.White)
}

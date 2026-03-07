package com.cdodi

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import com.cdodi.components.*
import com.cdodi.pages.AboutPage
import com.cdodi.pages.BoidsPage
import com.cdodi.pages.GameOfLifePage
import com.cdodi.pages.SmallScreenPage
import kotlinx.browser.document

enum class Screen {
    Home,
    About,
    Boids,
    GameOfLife,
}

fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}

@Composable
private fun App() {
    val runtimeShader by rememberShader("bokeh")

    MaterialTheme {
        LookaheadScope {
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .backgroundShader(runtimeShader)
            ) {
                if (LocalIsSmallWindow.current) {
                    SmallScreenPage()
                } else {
                    AppContent()
                }
            }
        }
    }
}

@Composable
private fun LookaheadScope.AppContent() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val cardModifier = Modifier.size(15.vw)
    val homeButton = movableCard(
        text = "Home",
        onClick = { currentScreen = Screen.Home }
    )
    val aboutButton = movableCard(
        text = "About",
        onClick = { currentScreen = Screen.About }
    )
    val contactsButton = movableCard(
        text = "Boids",
        onClick = { currentScreen = Screen.Boids }
    )
    val sketchButton = movableCard(
        text = "Game Of Life",
        onClick = { currentScreen = Screen.GameOfLife }
    )
    val bodyCard = movableBodyCard()

    Box(contentAlignment = Alignment.Center,) {
        if (currentScreen == Screen.Home) {
            MainMenu(
                body = {
                    bodyCard(Modifier.size(20.vw).align(Alignment.Center), MorphingShape.Rhombus) {
                        Text(
                            text = "Welcome",
                            fontSize = 30.sp, color = Color(0xa0_5a_d6_ff),
                        )
                    }
                },
                menuContent = {
                    homeButton(cardModifier.align(Alignment.TopStart), MorphingShape.TriangleTopStart)
                    aboutButton(cardModifier.align(Alignment.TopEnd), MorphingShape.TriangleTopEnd)
                    contactsButton(cardModifier.align(Alignment.BottomStart), MorphingShape.TriangleBottomStart)
                    sketchButton(cardModifier.align(Alignment.BottomEnd), MorphingShape.TriangleBottomEnd)
                },
            )
        } else {
            TopBarForm {
                homeButton(topBarModifier, MorphingShape.Rectangle)
                aboutButton(topBarModifier, MorphingShape.Rectangle)
                contactsButton(topBarModifier, MorphingShape.Rectangle)
                sketchButton(topBarModifier, MorphingShape.Rectangle)
            }

            bodyCard(
                Modifier.fillMaxWidth().height(85f.vh).align(Alignment.BottomCenter),
                MorphingShape.Rectangle
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    contentKey = { it },
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            slideInHorizontally(tween(400)) { width -> width } togetherWith
                                    slideOutHorizontally(tween(400)) { width -> -width }
                        } else {
                            slideInHorizontally(tween(400)) { width -> -width } togetherWith
                                    slideOutHorizontally(tween(400)) { width -> width }
                        }
                    }
                ) {
                    when(currentScreen) {
                        Screen.About -> AboutPage()
                        Screen.Boids -> BoidsPage()
                        Screen.GameOfLife -> GameOfLifePage()
                        Screen.Home -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.TopBarForm(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.align(Alignment.TopCenter),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

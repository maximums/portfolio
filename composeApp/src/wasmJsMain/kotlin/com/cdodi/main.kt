package com.cdodi

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import blog.composeapp.generated.resources.Res
import blog.composeapp.generated.resources.desktop_transparent
import blog.composeapp.generated.resources.laptop
import com.cdodi.components.*
import com.cdodi.pages.GameOfLifePage
import kotlinx.browser.document
import org.jetbrains.compose.resources.painterResource

sealed class Screen {
    data object Home : Screen()
    data object About : Screen()
    data object Contacts : Screen()
    data object GameOfLife : Screen()
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
private fun SmallScreenPage() {
    val desktopImg = painterResource(Res.drawable.desktop_transparent)
    val laptopImg = painterResource(Res.drawable.laptop)

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.width(80.vw)
        ) {
            Image(
                painter = desktopImg,
                contentDescription = "Image of Desktop",
                contentScale = ContentScale.Fit,
                alpha = 0.8f,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(),
            )
            Image(
                painter = laptopImg,
                contentDescription = "Image of laptop",
                contentScale = ContentScale.Fit,
                alpha = 0.8f,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(.4f),
            )
        }

        Text(
            text = "For full experience please open on larger screens",
            color = Color.White,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun LookaheadScope.AppContent() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
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
        text = "Contacts",
        onClick = { currentScreen = Screen.Contacts }
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
                Modifier.fillMaxWidth().height(1000.dp).align(Alignment.BottomCenter),
                MorphingShape.Rectangle
            ) {
                GameOfLifePage()
            }
        }
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

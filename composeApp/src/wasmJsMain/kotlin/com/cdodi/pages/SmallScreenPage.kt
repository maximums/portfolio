package com.cdodi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blog.composeapp.generated.resources.Res
import blog.composeapp.generated.resources.desktop_transparent
import blog.composeapp.generated.resources.laptop
import com.cdodi.vw
import org.jetbrains.compose.resources.painterResource

@Composable
fun SmallScreenPage() {
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
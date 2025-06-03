package com.example.proyecto.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import kotlin.random.Random
@Composable
fun RainEffect(isRaining: Boolean) {
    if (!isRaining) return

    val rainDrops = remember { (1..100).map { RainDrop() } }

    Box(modifier = Modifier.fillMaxSize()) {
        rainDrops.forEach { drop ->
            key(drop.id) {
                val infiniteTransition = rememberInfiniteTransition()
                val yPosition by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(drop.speed, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val xPosition by infiniteTransition.animateFloat(
                    initialValue = drop.startX,
                    targetValue = drop.startX + drop.drift,
                    animationSpec = infiniteRepeatable(
                        animation = tween(drop.speed * 2, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawLine(
                        color = Color(0xFFA0C4E0),
                        start = Offset(
                            x = xPosition * size.width,
                            y = yPosition * size.height
                        ),
                        end = Offset(
                            x = (xPosition * size.width) + drop.length * 0.1f,
                            y = (yPosition * size.height) + drop.length
                        ),
                        strokeWidth = drop.width,
                        alpha = 0.7f
                    )
                }
            }
        }
    }
}

private data class RainDrop(
    val id: Int = Random.nextInt(),
    val startX: Float = Random.nextFloat(),
    val speed: Int = Random.nextInt(800, 2000),
    val length: Float = Random.nextFloat() * 10 + 10,
    val width: Float = Random.nextFloat() * 1 + 0.5f,
    val drift: Float = Random.nextFloat() * 0.1f - 0.05f
)
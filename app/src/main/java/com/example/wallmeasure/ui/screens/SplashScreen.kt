package com.example.wallmeasure.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallmeasure.ui.theme.DimensionLineColorLight
import com.example.wallmeasure.ui.theme.SlatePrimaryLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
        delay(150)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "WallMeasure",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Calculate wall surface area quickly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Architectural Splash Diagram with measuring-line animation
            val currentProgress = progress.value
            val primaryColor = MaterialTheme.colorScheme.primary
            val textColor = MaterialTheme.colorScheme.onSurface

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val canvasW = size.width
                val canvasH = size.height

                val wallW = (canvasW * 0.65f) * currentProgress
                val wallH = (canvasH * 0.50f) * currentProgress

                val left = (canvasW - wallW) / 2f
                val top = (canvasH - wallH) / 2f

                // Measuring line top animation
                val arrowSize = 6.dp.toPx()
                val lineCol = DimensionLineColorLight

                if (currentProgress > 0.3f) {
                    val horizLineW = wallW
                    val hxStart = (canvasW - horizLineW) / 2f
                    val hxEnd = hxStart + horizLineW
                    val hy = top - 18f

                    drawLine(
                        color = lineCol,
                        start = Offset(hxStart, hy),
                        end = Offset(hxEnd, hy),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Dimension label: ← 500 cm →
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            this.color = textColor.hashCode()
                            this.textSize = 30f
                            this.textAlign = android.graphics.Paint.Align.CENTER
                            this.isFakeBoldText = true
                        }
                        drawText("← 500 cm →", canvasW / 2f, hy - 8f, paint)
                    }
                }

                // Wall Blueprint Box
                drawRect(
                    color = primaryColor.copy(alpha = 0.12f * currentProgress),
                    topLeft = Offset(left, top),
                    size = Size(wallW, wallH)
                )
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(left, top),
                    size = Size(wallW, wallH),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Vertical line & label: 300 cm
                if (currentProgress > 0.6f) {
                    val vyStart = top
                    val vyEnd = top + wallH
                    val vx = left - 18f

                    drawLine(
                        color = lineCol,
                        start = Offset(vx, vyStart),
                        end = Offset(vx, vyEnd),
                        strokeWidth = 2.dp.toPx()
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            this.color = textColor.hashCode()
                            this.textSize = 28f
                            this.textAlign = android.graphics.Paint.Align.RIGHT
                            this.isFakeBoldText = true
                        }
                        drawText("300 cm", vx - 8f, (vyStart + vyEnd) / 2f + 8f, paint)
                    }
                }

                // Inner label: WALL AREA
                if (currentProgress > 0.8f) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            this.color = primaryColor.hashCode()
                            this.textSize = 28f
                            this.textAlign = android.graphics.Paint.Align.CENTER
                            this.isFakeBoldText = true
                        }
                        drawText("WALL AREA", canvasW / 2f, canvasH / 2f + 8f, paint)
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "WALLMEASURE — MEASURE THE WALL. KNOW THE AREA.",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
        }
    }
}

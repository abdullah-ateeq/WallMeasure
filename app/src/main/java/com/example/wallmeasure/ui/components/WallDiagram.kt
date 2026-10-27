package com.example.wallmeasure.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallmeasure.calculator.UnitConverter
import com.example.wallmeasure.model.AreaUnitType
import com.example.wallmeasure.model.Opening
import com.example.wallmeasure.model.OpeningType
import com.example.wallmeasure.model.UnitType
import com.example.wallmeasure.model.WallOrientation
import com.example.wallmeasure.ui.theme.DimensionLineColorDark
import com.example.wallmeasure.ui.theme.DimensionLineColorLight
import com.example.wallmeasure.ui.theme.DoorColor
import com.example.wallmeasure.ui.theme.OtherOpeningColor
import com.example.wallmeasure.ui.theme.WallFillDark
import com.example.wallmeasure.ui.theme.WallFillLight
import com.example.wallmeasure.ui.theme.WallStrokeDark
import com.example.wallmeasure.ui.theme.WallStrokeLight
import com.example.wallmeasure.ui.theme.WindowColor
import kotlin.math.max
import kotlin.math.min

@Composable
fun WallDiagram(
    widthMm: Double,
    heightMm: Double,
    openings: List<Opening>,
    unit: UnitType,
    outputUnit: AreaUnitType,
    orientation: WallOrientation = WallOrientation.HORIZONTAL,
    showOpenings: Boolean = true,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f
    val wallFill = if (isDark) WallFillDark else WallFillLight
    val wallStroke = if (isDark) WallStrokeDark else WallStrokeLight
    val dimensionLineColor = if (isDark) DimensionLineColorDark else DimensionLineColorLight
    val textColor = MaterialTheme.colorScheme.onSurface

    val displayedWidthMm = if (orientation == WallOrientation.HORIZONTAL) widthMm else heightMm
    val displayedHeightMm = if (orientation == WallOrientation.HORIZONTAL) heightMm else widthMm

    val topLabel = UnitConverter.formatDimension(displayedWidthMm, unit, decimalPlaces)
    val sideLabel = UnitConverter.formatDimension(displayedHeightMm, unit, decimalPlaces)
    val areaM2Text = UnitConverter.formatArea(widthMm * heightMm, outputUnit, decimalPlaces)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            val density = LocalDensity.current
            val maxWidthPx = with(density) { maxWidth.toPx() }
            val maxHeightPx = with(density) { maxHeight.toPx() }

            // Margins for dimension lines and labels
            val leftMargin = 55f
            val rightMargin = 20f
            val topMargin = 40f
            val bottomMargin = 20f

            val availableWidth = max(20f, maxWidthPx - leftMargin - rightMargin)
            val availableHeight = max(20f, maxHeightPx - topMargin - bottomMargin)

            // Auto-scale maintaining exact aspect ratio
            val scale = min(
                availableWidth / max(1.0, displayedWidthMm).toFloat(),
                availableHeight / max(1.0, displayedHeightMm).toFloat()
            )

            val wallDrawWidth = (displayedWidthMm * scale).toFloat()
            val wallDrawHeight = (displayedHeightMm * scale).toFloat()

            val wallLeft = leftMargin + (availableWidth - wallDrawWidth) / 2f
            val wallTop = topMargin + (availableHeight - wallDrawHeight) / 2f

            Canvas(modifier = Modifier.matchParentSize()) {
                // 1. Draw Wall Rectangle
                drawRect(
                    color = wallFill,
                    topLeft = Offset(wallLeft, wallTop),
                    size = Size(wallDrawWidth, wallDrawHeight)
                )
                drawRect(
                    color = wallStroke,
                    topLeft = Offset(wallLeft, wallTop),
                    size = Size(wallDrawWidth, wallDrawHeight),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 2. Draw Dimension Lines (Top and Left)
                drawDimensionLineHorizontal(
                    xStart = wallLeft,
                    xEnd = wallLeft + wallDrawWidth,
                    y = wallTop - 18f,
                    label = topLabel,
                    color = dimensionLineColor,
                    labelColor = textColor
                )

                drawDimensionLineVertical(
                    yStart = wallTop,
                    yEnd = wallTop + wallDrawHeight,
                    x = wallLeft - 18f,
                    label = sideLabel,
                    color = dimensionLineColor,
                    labelColor = textColor
                )

                // 3. Draw Openings if enabled
                if (showOpenings && openings.isNotEmpty() && orientation == WallOrientation.HORIZONTAL) {
                    drawOpeningsOnWall(
                        openings = openings,
                        wallLeft = wallLeft,
                        wallTop = wallTop,
                        wallDrawWidth = wallDrawWidth,
                        wallDrawHeight = wallDrawHeight,
                        wallWidthMm = widthMm,
                        wallHeightMm = heightMm,
                        scale = scale
                    )
                }
            }

            // Center Wall Area Text overlay
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WALL AREA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = wallStroke,
                    letterSpacing = 1.sp
                )
                Text(
                    text = areaM2Text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun DrawScope.drawDimensionLineHorizontal(
    xStart: Float,
    xEnd: Float,
    y: Float,
    label: String,
    color: Color,
    labelColor: Color
) {
    val arrowSize = 6.dp.toPx()
    // Main horizontal line
    drawLine(
        color = color,
        start = Offset(xStart, y),
        end = Offset(xEnd, y),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
    )
    // Left marker / arrow
    drawLine(color = color, start = Offset(xStart, y - arrowSize), end = Offset(xStart, y + arrowSize), strokeWidth = 1.5.dp.toPx())
    val leftArrow = Path().apply {
        moveTo(xStart, y)
        lineTo(xStart + arrowSize, y - arrowSize / 2f)
        lineTo(xStart + arrowSize, y + arrowSize / 2f)
        close()
    }
    drawPath(leftArrow, color = color)

    // Right marker / arrow
    drawLine(color = color, start = Offset(xEnd, y - arrowSize), end = Offset(xEnd, y + arrowSize), strokeWidth = 1.5.dp.toPx())
    val rightArrow = Path().apply {
        moveTo(xEnd, y)
        lineTo(xEnd - arrowSize, y - arrowSize / 2f)
        lineTo(xEnd - arrowSize, y + arrowSize / 2f)
        close()
    }
    drawPath(rightArrow, color = color)

    // Label on top
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color = labelColor.hashCode()
            this.textSize = 28f
            this.textAlign = android.graphics.Paint.Align.CENTER
            this.isFakeBoldText = true
        }
        drawText(label, (xStart + xEnd) / 2f, y - 8f, paint)
    }
}

private fun DrawScope.drawDimensionLineVertical(
    yStart: Float,
    yEnd: Float,
    x: Float,
    label: String,
    color: Color,
    labelColor: Color
) {
    val arrowSize = 6.dp.toPx()
    // Main vertical line
    drawLine(
        color = color,
        start = Offset(x, yStart),
        end = Offset(x, yEnd),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
    )
    // Top marker / arrow
    drawLine(color = color, start = Offset(x - arrowSize, yStart), end = Offset(x + arrowSize, yStart), strokeWidth = 1.5.dp.toPx())
    val topArrow = Path().apply {
        moveTo(x, yStart)
        lineTo(x - arrowSize / 2f, yStart + arrowSize)
        lineTo(x + arrowSize / 2f, yStart + arrowSize)
        close()
    }
    drawPath(topArrow, color = color)

    // Bottom marker / arrow
    drawLine(color = color, start = Offset(x - arrowSize, yEnd), end = Offset(x + arrowSize, yEnd), strokeWidth = 1.5.dp.toPx())
    val bottomArrow = Path().apply {
        moveTo(x, yEnd)
        lineTo(x - arrowSize / 2f, yEnd - arrowSize)
        lineTo(x + arrowSize / 2f, yEnd - arrowSize)
        close()
    }
    drawPath(bottomArrow, color = color)

    // Label on left
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color = labelColor.hashCode()
            this.textSize = 26f
            this.textAlign = android.graphics.Paint.Align.RIGHT
            this.isFakeBoldText = true
        }
        drawText(label, x - 10f, (yStart + yEnd) / 2f + 8f, paint)
    }
}

private fun DrawScope.drawOpeningsOnWall(
    openings: List<Opening>,
    wallLeft: Float,
    wallTop: Float,
    wallDrawWidth: Float,
    wallDrawHeight: Float,
    wallWidthMm: Double,
    wallHeightMm: Double,
    scale: Float
) {
    val totalSlots = openings.size
    openings.forEachIndexed { index, op ->
        val opDrawW = (op.widthMm * scale).toFloat().coerceAtMost(wallDrawWidth)
        val opDrawH = (op.heightMm * scale).toFloat().coerceAtMost(wallDrawHeight)

        // Calculate placement: If manual positionMm is set, use it; else distribute evenly across the wall
        val opLeft = if (op.positionMm != null) {
            wallLeft + (op.positionMm * scale).toFloat()
        } else {
            val step = wallDrawWidth / (totalSlots + 1)
            wallLeft + (step * (index + 1)) - (opDrawW / 2f)
        }.coerceIn(wallLeft, wallLeft + wallDrawWidth - opDrawW)

        val opTop = if (op.verticalPositionMm != null) {
            // Measured from floor up
            wallTop + wallDrawHeight - (op.verticalPositionMm * scale).toFloat() - opDrawH
        } else {
            when (op.type) {
                OpeningType.DOOR -> wallTop + wallDrawHeight - opDrawH // sits on floor
                OpeningType.WINDOW -> wallTop + (wallDrawHeight * 0.25f) // upper-middle
                OpeningType.OTHER -> wallTop + (wallDrawHeight * 0.35f)
            }
        }.coerceIn(wallTop, wallTop + wallDrawHeight - opDrawH)

        val color = when (op.type) {
            OpeningType.DOOR -> DoorColor
            OpeningType.WINDOW -> WindowColor
            OpeningType.OTHER -> OtherOpeningColor
        }

        // Draw opening rectangle
        drawRect(
            color = color.copy(alpha = 0.25f),
            topLeft = Offset(opLeft, opTop),
            size = Size(opDrawW, opDrawH)
        )
        drawRect(
            color = color,
            topLeft = Offset(opLeft, opTop),
            size = Size(opDrawW, opDrawH),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // If window, draw cross window panes
        if (op.type == OpeningType.WINDOW && opDrawW > 24f && opDrawH > 24f) {
            val midX = opLeft + opDrawW / 2f
            val midY = opTop + opDrawH / 2f
            drawLine(color = color.copy(alpha = 0.6f), start = Offset(midX, opTop), end = Offset(midX, opTop + opDrawH), strokeWidth = 1f)
            drawLine(color = color.copy(alpha = 0.6f), start = Offset(opLeft, midY), end = Offset(opLeft + opDrawW, midY), strokeWidth = 1f)
        }

        // Label opening inside or above
        if (opDrawW > 30f && opDrawH > 20f) {
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    this.color = color.hashCode()
                    this.textSize = 20f
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    this.isFakeBoldText = true
                }
                val label = if (op.quantity > 1) "${op.name} (${op.quantity})" else op.name
                drawText(label, opLeft + opDrawW / 2f, opTop + opDrawH / 2f + 6f, paint)
            }
        }
    }
}

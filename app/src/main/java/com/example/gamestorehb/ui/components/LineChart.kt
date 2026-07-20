package com.example.gamestorehb.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Minimalist Line Chart using Compose Canvas.
 * Automatically scales the data points to fit the available space.
 */
@Composable
fun LineChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 6f
) {
    if (data.isEmpty()) return

    val maxVal = data.maxOrNull() ?: 1.0
    val minVal = data.minOrNull() ?: 0.0
    val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height

        val stepX = width / (if (data.size > 1) data.size - 1 else 1)
        
        val path = Path()
        
        data.forEachIndexed { index, value ->
            val x = index * stepX
            // Y is inverted in Canvas (0 is top)
            val y = height - (((value - minVal) / range) * height).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

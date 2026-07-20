package com.example.gamestorehb.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Minimalist Horizontal Stacked Bar Chart to show allocation weights.
 */
@Composable
fun AllocationBarChart(
    allocations: List<Double>,
    modifier: Modifier = Modifier
) {
    if (allocations.isEmpty()) return

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val width = size.width
        val height = size.height
        
        var currentX = 0f
        
        allocations.forEachIndexed { index, percentage ->
            val segmentWidth = (percentage / 100.0).toFloat() * width
            if (segmentWidth > 0) {
                drawRoundRect(
                    color = colors[index % colors.size],
                    topLeft = Offset(currentX, 0f),
                    size = Size(segmentWidth, height),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                currentX += segmentWidth
            }
        }
    }
}

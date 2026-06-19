package com.example.money2.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

data class SpotlightTarget(
    val rect: Rect,
    val title: String,
    val description: String
)

@Composable
fun SpotlightTour(
    targets: List<SpotlightTarget>,
    currentTargetIndex: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    if (targets.isEmpty() || currentTargetIndex >= targets.size) return
    val target = targets[currentTargetIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onNext() }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.99f }
        ) {
            // Draw dark overlay
            drawRect(color = Color.Black.copy(alpha = 0.7f))

            // Cutout
            val padding = 16.dp.toPx()
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(target.rect.left - padding, target.rect.top - padding),
                size = Size(target.rect.width + padding * 2, target.rect.height + padding * 2),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                blendMode = BlendMode.Clear
            )
        }

        // Draw text near the target
        val density = LocalDensity.current
        val targetTopDp = with(density) { target.rect.top.toDp() }
        val targetBottomDp = with(density) { target.rect.bottom.toDp() }
        val screenHeight = with(density) { LocalContext.current.resources.displayMetrics.heightPixels.toDp() }
        
        val isBottomHalf = target.rect.center.y > (with(density) { screenHeight.toPx() } / 2)
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .offset(y = if (isBottomHalf) targetTopDp - 120.dp else targetBottomDp + 32.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = target.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = target.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

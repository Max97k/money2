package com.example.money2.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    val speed: Float,
    val size: Float,
    val initialAngleX: Float,
    val initialAngleY: Float
)

@Composable
fun DynamicBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    
    // Create particles once
    val numParticles = 150
    val particles = remember {
        Array(numParticles) {
            Particle3D(
                x = Random.nextFloat() * 2000f - 1000f,
                y = Random.nextFloat() * 2000f - 1000f,
                z = Random.nextFloat() * 2000f,
                speed = Random.nextFloat() * 0.5f + 0.1f, // Very slow natural drift
                size = Random.nextFloat() * 2f + 0.5f,
                initialAngleX = Random.nextFloat() * Math.PI.toFloat() * 2f,
                initialAngleY = Random.nextFloat() * Math.PI.toFloat() * 2f
            )
        }
    }
    
    var time by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        var lastTime = withFrameNanos { it }
        while (isActive) {
            val currentTime = withFrameNanos { it }
            val dt = (currentTime - lastTime) / 1_000_000_000f // seconds
            lastTime = currentTime
            time += dt
        }
    }

    val bgColor = MaterialTheme.colorScheme.background
    val particleColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width == 0 || size.height == 0) return@Canvas
            
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val fov = 800f
            
            // Global rotation based on time
            val rotX = time * 0.05f
            val rotY = time * 0.03f
            
            val cosX = cos(rotX)
            val sinX = sin(rotX)
            val cosY = cos(rotY)
            val sinY = sin(rotY)
            
            particles.forEach { p ->
                // Drift particles slowly
                p.z -= p.speed * 20f * (16f / 1000f) // approximate 60fps drift
                if (p.z < 0) {
                    p.z += 2000f
                    p.x = Random.nextFloat() * 2000f - 1000f
                    p.y = Random.nextFloat() * 2000f - 1000f
                }
                
                // Slight natural oscillation
                val ox = p.x + cos(time + p.initialAngleX) * 20f
                val oy = p.y + sin(time + p.initialAngleY) * 20f
                val oz = p.z
                
                // 3D Rotation Y
                val rx = ox * cosY - oz * sinY
                val rz1 = ox * sinY + oz * cosY
                
                // 3D Rotation X
                val ry = oy * cosX - rz1 * sinX
                val rz = oy * sinX + rz1 * cosX
                
                // Only draw if in front of camera
                val finalZ = rz + 1000f // push back so camera is at origin
                if (finalZ > 0) {
                    val scale = fov / (fov + finalZ)
                    val px = centerX + rx * scale
                    val py = centerY + ry * scale
                    
                    // Fade out in distance and very close
                    val alpha = (1f - (finalZ / 3000f)).coerceIn(0f, 1f) * 
                               (if (finalZ < 200f) finalZ / 200f else 1f)
                    
                    if (px in 0f..size.width.toFloat() && py in 0f..size.height.toFloat()) {
                        drawCircle(
                            color = particleColor.copy(alpha = alpha * 0.7f),
                            radius = p.size * scale * 2f,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }
        
        // Foreground content
        content()
    }
}

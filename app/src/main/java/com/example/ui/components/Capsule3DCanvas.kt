package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Interactive 3D Pharmacy Capsule & Molecular Atom simulation.
 * Responds to drag touch gestures with smooth 3D rotation, depth shading, and floating micro-particles.
 */
@Composable
fun Capsule3DCanvas(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary
) {
    var manualRotX by remember { mutableFloatStateOf(15f) }
    var manualRotY by remember { mutableFloatStateOf(25f) }

    val infiniteTransition = rememberInfiniteTransition(label = "3DAutoRotation")
    val autoAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "autoAngle"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingY"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    manualRotY = (manualRotY + dragAmount.x * 0.4f) % 360f
                    manualRotX = (manualRotX - dragAmount.y * 0.3f).coerceIn(-45f, 45f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f + floatingOffset
            val currentRotY = (manualRotY + autoAngle) % 360f
            val radY = Math.toRadians(currentRotY.toDouble())
            val radX = Math.toRadians(manualRotX.toDouble())

            // 1. Draw Ambient Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.22f),
                        secondaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = size.width * 0.45f
                ),
                radius = size.width * 0.45f,
                center = Offset(cx, cy)
            )

            // 2. Draw Floating Molecular Ions / Atmospheric Particles
            val particleCount = 14
            for (i in 0 until particleCount) {
                val pAngle = Math.toRadians((i * (360.0 / particleCount) + autoAngle * 0.6).toDouble())
                val pDist = (size.width * 0.34f) + sin(pAngle * 2 + i).toFloat() * 18f
                val px = cx + cos(pAngle).toFloat() * pDist
                val py = cy + sin(pAngle * 0.8 + i).toFloat() * (pDist * 0.38f)
                val pRadius = 2.5f + (sin(pAngle * 3).toFloat() + 1f) * 1.5f
                val alpha = (0.25f + sin(pAngle + i).toFloat() * 0.25f).coerceIn(0.1f, 0.7f)

                drawCircle(
                    color = if (i % 2 == 0) primaryColor.copy(alpha = alpha) else secondaryColor.copy(alpha = alpha),
                    radius = pRadius,
                    center = Offset(px, py)
                )
            }

            // 3. Draw 3D Molecular Orbital Ring (Back Half)
            val ringRadiusX = size.width * 0.36f
            val ringRadiusY = 32f
            drawEllipticalOrbit(
                cx = cx,
                cy = cy,
                rx = ringRadiusX,
                ry = ringRadiusY,
                angleDeg = -22f + manualRotX * 0.2f,
                color = primaryColor.copy(alpha = 0.35f),
                isBack = true
            )

            // 4. Draw 3D Pharmaceutical Capsule
            // Capsule dimensions
            val capWidth = 72f
            val capHeight = 130f
            val capCorner = 36f

            // Compute pseudo-3D rotation tilt
            val tiltAngle = (-30f + sin(radY).toFloat() * 18f)

            rotate(degrees = tiltAngle, pivot = Offset(cx, cy)) {
                // Capsule Drop Shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.35f),
                    topLeft = Offset(cx - capWidth / 2f + 6f, cy - capHeight / 2f + 10f),
                    size = androidx.compose.ui.geometry.Size(capWidth, capHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(capCorner, capCorner)
                )

                // Top Half Shell (Primary Color / Active Compound)
                val topPath = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = cx - capWidth / 2f,
                            top = cy - capHeight / 2f,
                            right = cx + capWidth / 2f,
                            bottom = cy,
                            topLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(capCorner, capCorner),
                            topRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(capCorner, capCorner),
                            bottomLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(0f, 0f),
                            bottomRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(0f, 0f)
                        )
                    )
                }

                drawPath(
                    path = topPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.85f),
                            primaryColor,
                            primaryColor.copy(alpha = 0.7f)
                        ),
                        startX = cx - capWidth / 2f,
                        endX = cx + capWidth / 2f
                    )
                )

                // Bottom Half Shell (Secondary / Sustained Release Base)
                val bottomPath = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = cx - capWidth / 2f,
                            top = cy,
                            right = cx + capWidth / 2f,
                            bottom = cy + capHeight / 2f,
                            topLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(0f, 0f),
                            topRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(0f, 0f),
                            bottomLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(capCorner, capCorner),
                            bottomRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(capCorner, capCorner)
                        )
                    )
                }

                drawPath(
                    path = bottomPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.8f),
                            secondaryColor,
                            secondaryColor.copy(alpha = 0.65f)
                        ),
                        startX = cx - capWidth / 2f,
                        endX = cx + capWidth / 2f
                    )
                )

                // Center Metallic Joining Band
                drawRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(cx - capWidth / 2f, cy - 3.5f),
                    size = androidx.compose.ui.geometry.Size(capWidth, 7f)
                )

                // 3D Specular Highlight Gloss Streak (simulating curved glass/gelatin)
                val glossPath = Path().apply {
                    moveTo(cx - capWidth * 0.32f, cy - capHeight * 0.42f)
                    lineTo(cx - capWidth * 0.32f, cy + capHeight * 0.42f)
                }
                drawPath(
                    path = glossPath,
                    color = Color.White.copy(alpha = 0.45f),
                    style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                )

                // Emblem: Pharmacy Cross on Top Shell
                val crossSize = 14f
                val crossThick = 4f
                val crossY = cy - capHeight * 0.25f

                // Horizontal bar
                drawRect(
                    color = Color.White.copy(alpha = 0.95f),
                    topLeft = Offset(cx - crossSize / 2f, crossY - crossThick / 2f),
                    size = androidx.compose.ui.geometry.Size(crossSize, crossThick)
                )
                // Vertical bar
                drawRect(
                    color = Color.White.copy(alpha = 0.95f),
                    topLeft = Offset(cx - crossThick / 2f, crossY - crossSize / 2f),
                    size = androidx.compose.ui.geometry.Size(crossThick, crossSize)
                )
            }

            // 5. Draw 3D Molecular Orbital Ring (Front Half with Orbiting Atoms)
            drawEllipticalOrbit(
                cx = cx,
                cy = cy,
                rx = ringRadiusX,
                ry = ringRadiusY,
                angleDeg = -22f + manualRotX * 0.2f,
                color = accentColor.copy(alpha = 0.7f),
                isBack = false
            )

            // Orbiting Active Electron Atom
            val atomAngle = Math.toRadians((autoAngle * 1.8).toDouble())
            val atomTilt = Math.toRadians((-22.0 + manualRotX * 0.2))
            val rawAx = cos(atomAngle).toFloat() * ringRadiusX
            val rawAy = sin(atomAngle).toFloat() * ringRadiusY
            val finalAx = cx + (rawAx * cos(atomTilt) - rawAy * sin(atomTilt)).toFloat()
            val finalAy = cy + (rawAx * sin(atomTilt) + rawAy * cos(atomTilt)).toFloat()

            // Atom Glow & Core
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = 12f,
                center = Offset(finalAx, finalAy)
            )
            drawCircle(
                color = Color.White,
                radius = 5.5f,
                center = Offset(finalAx, finalAy)
            )
        }
    }
}

private fun DrawScope.drawEllipticalOrbit(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    angleDeg: Float,
    color: Color,
    isBack: Boolean
) {
    rotate(degrees = angleDeg, pivot = Offset(cx, cy)) {
        val path = Path().apply {
            val startDeg = if (isBack) 180f else 0f
            val sweepDeg = 180f
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = cx - rx,
                    top = cy - ry,
                    right = cx + rx,
                    bottom = cy + ry
                ),
                startAngleDegrees = startDeg,
                sweepAngleDegrees = sweepDeg,
                forceMoveTo = true
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = if (isBack) 2.2f else 3.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        )
    }
}

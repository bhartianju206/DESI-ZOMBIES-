package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 106.dp,
    baseColor: Color = Color(0x3300E676),
    knobColor: Color = Color(0xFF00E676),
    isEnabled: Boolean = true,
    testTag: String = "virtual_joystick",
    onValueChange: (x: Float, y: Float) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(isEnabled) {
        if (!isEnabled) {
            thumbOffset = Offset.Zero
            onValueChange(0f, 0f)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag)
            .pointerInput(isEnabled) {
                if (!isEnabled) return@pointerInput
                val radius = this.size.width / 2f
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(radius, radius)
                        val delta = offset - center
                        val dist = hypot(delta.x, delta.y)
                        val maxDist = radius * 0.75f
                        val clampedOffset = if (dist > maxDist) {
                            val angle = atan2(delta.y, delta.x)
                            Offset(cos(angle) * maxDist, sin(angle) * maxDist)
                        } else {
                            delta
                        }
                        thumbOffset = clampedOffset
                        onValueChange(clampedOffset.x / maxDist, clampedOffset.y / maxDist)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(radius, radius)
                        val delta = change.position - center
                        val dist = hypot(delta.x, delta.y)
                        val maxDist = radius * 0.75f
                        val clampedOffset = if (dist > maxDist) {
                            val angle = atan2(delta.y, delta.x)
                            Offset(cos(angle) * maxDist, sin(angle) * maxDist)
                        } else {
                            delta
                        }
                        thumbOffset = clampedOffset
                        onValueChange(clampedOffset.x / maxDist, clampedOffset.y / maxDist)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onValueChange(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onValueChange(0f, 0f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = this.size.width / 2f
            val knobRadius = outerRadius * 0.35f

            // Translucent base
            drawCircle(
                color = baseColor,
                radius = outerRadius - 3f,
                center = center
            )
            // Glowing neon boundary ring
            drawCircle(
                color = knobColor.copy(alpha = 0.55f),
                radius = outerRadius - 3f,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )
            // Cardinal direction crosshair ticks
            val tickLen = 9.dp.toPx()
            drawLine(
                color = knobColor.copy(alpha = 0.45f),
                start = Offset(center.x, 6.dp.toPx()),
                end = Offset(center.x, 6.dp.toPx() + tickLen),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = knobColor.copy(alpha = 0.45f),
                start = Offset(center.x, this.size.height - 6.dp.toPx()),
                end = Offset(center.x, this.size.height - 6.dp.toPx() - tickLen),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = knobColor.copy(alpha = 0.45f),
                start = Offset(6.dp.toPx(), center.y),
                end = Offset(6.dp.toPx() + tickLen, center.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = knobColor.copy(alpha = 0.45f),
                start = Offset(this.size.width - 6.dp.toPx(), center.y),
                end = Offset(this.size.width - 6.dp.toPx() - tickLen, center.y),
                strokeWidth = 2.dp.toPx()
            )

            // Tactile Thumb Knob
            val knobCenter = center + thumbOffset
            drawCircle(
                color = knobColor.copy(alpha = 0.85f),
                radius = knobRadius,
                center = knobCenter
            )
            drawCircle(
                color = Color.White,
                radius = knobRadius * 0.4f,
                center = knobCenter
            )
        }
    }
}

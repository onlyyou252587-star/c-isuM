package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WaveformInactive

@Composable
fun WaveformSeekBar(
  progress: Float,
  formattedPosition: String,
  formattedDuration: String,
  onSeek: (Float) -> Unit,
  modifier: Modifier = Modifier,
  activeColor: Color = NeonCyan,
  inactiveColor: Color = WaveformInactive
) {
  var isDragging by remember { mutableStateOf(false) }
  var dragProgress by remember { mutableFloatStateOf(progress) }

  val displayProgress = if (isDragging) dragProgress else progress

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("waveform_seek_bar")
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
        .pointerInput(Unit) {
          detectTapGestures { offset ->
            val p = (offset.x / size.width).coerceIn(0f, 1f)
            onSeek(p)
          }
        }
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { offset ->
              isDragging = true
              dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
            },
            onDragEnd = {
              isDragging = false
              onSeek(dragProgress)
            },
            onDragCancel = {
              isDragging = false
            },
            onDrag = { change, _ ->
              change.consume()
              dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
            }
          )
        }
    ) {
      val canvasWidth = size.width
      val canvasHeight = size.height

      val barCount = 48
      val spacing = 3.dp.toPx()
      val totalSpacing = spacing * (barCount - 1)
      val barWidth = ((canvasWidth - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

      // Procedural waveform envelope for track shape
      for (i in 0 until barCount) {
        val normIndex = i.toFloat() / barCount
        // Pseudo-waveform curve: higher in chorus/middle, dynamic variations
        val variation = kotlin.math.sin(normIndex * 12f) * 0.25f + kotlin.math.cos(normIndex * 5f) * 0.2f
        val envelope = (0.35f + kotlin.math.sin(normIndex * kotlin.math.PI.toFloat()) * 0.45f + variation).coerceIn(0.15f, 0.95f)

        val barHeight = envelope * canvasHeight
        val x = i * (barWidth + spacing)
        val y = (canvasHeight - barHeight) / 2f

        val isPassed = normIndex <= displayProgress
        val color = if (isPassed) activeColor else inactiveColor

        drawRoundRect(
          color = color,
          topLeft = Offset(x, y),
          size = Size(barWidth, barHeight),
          cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
      }

      // Current Scrubber Thumb
      val thumbX = displayProgress * canvasWidth
      drawCircle(
        color = activeColor,
        radius = if (isDragging) 6.dp.toPx() else 4.dp.toPx(),
        center = Offset(thumbX, canvasHeight / 2f)
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = formattedPosition,
        color = TextMuted,
        fontSize = 12.sp
      )
      Text(
        text = formattedDuration,
        color = TextMuted,
        fontSize = 12.sp
      )
    }
  }
}

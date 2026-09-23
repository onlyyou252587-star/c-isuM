package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet

@Composable
fun AudioVisualizer(
  amplitudes: List<Float>,
  modifier: Modifier = Modifier,
  isPlaying: Boolean = true,
  barCount: Int = 20,
  accentColor: Color = NeonCyan
) {
  val visualizerBrush = Brush.verticalGradient(
    colors = listOf(
      NeonPink,
      NeonPurple,
      accentColor
    )
  )

  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(68.dp)
  ) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    if (amplitudes.isEmpty() || canvasWidth <= 0) return@Canvas

    val spacing = 4.dp.toPx()
    val totalSpacing = spacing * (barCount - 1)
    val barWidth = ((canvasWidth - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

    amplitudes.take(barCount).forEachIndexed { index, amp ->
      val animatedAmp = if (isPlaying) amp else 0.08f
      val barHeight = (animatedAmp * canvasHeight).coerceIn(4.dp.toPx(), canvasHeight)
      val x = index * (barWidth + spacing)
      val y = canvasHeight - barHeight

      // Main Spectrum Bar with rounded corners
      drawRoundRect(
        brush = visualizerBrush,
        topLeft = Offset(x, y),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
      )

      // Peak Cap line
      drawRoundRect(
        color = NeonPink,
        topLeft = Offset(x, (y - 3.dp.toPx()).coerceAtLeast(0f)),
        size = Size(barWidth, 2.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
      )
    }
  }
}

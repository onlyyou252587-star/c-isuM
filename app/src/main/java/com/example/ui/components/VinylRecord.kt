package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple

@Composable
fun VinylRecord(
  isPlaying: Boolean,
  bassLevelFactor: Float = 0.8f,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "VinylSpin")

  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 8000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "VinylRotation"
  )

  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 600, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "BassPulse"
  )

  val currentRotation = if (isPlaying) rotation else 0f
  val currentPulse = if (isPlaying) pulseScale else 1.0f

  Box(
    modifier = modifier
      .fillMaxWidth(0.72f)
      .aspectRatio(1f),
    contentAlignment = Alignment.Center
  ) {
    // Outer Sub-Bass Glow Canvas
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .rotate(currentRotation)
    ) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = (size.minDimension / 2f) * if (isPlaying) currentPulse else 1f

      if (isPlaying) {
        // Glowing aura rings
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(NeonCyan.copy(alpha = 0.25f), Color.Transparent),
            center = center,
            radius = radius * 1.15f
          ),
          center = center,
          radius = radius * 1.15f
        )
      }

      // Vinyl outer body
      drawCircle(
        color = Color(0xFF0F141E),
        center = center,
        radius = radius
      )

      // Grooves
      val grooveColor = Color(0xFF1E2838)
      val grooveHighlight = Color(0xFF334155)
      val rings = 7
      for (i in 1..rings) {
        val r = radius * (0.42f + (i.toFloat() / rings) * 0.52f)
        drawCircle(
          color = if (i % 2 == 0) grooveHighlight.copy(alpha = 0.3f) else grooveColor.copy(alpha = 0.4f),
          center = center,
          radius = r,
          style = Stroke(width = 1.5.dp.toPx())
        )
      }

      // Vinyl rim border
      drawCircle(
        color = NeonCyan.copy(alpha = if (isPlaying) 0.8f else 0.3f),
        center = center,
        radius = radius,
        style = Stroke(width = 2.dp.toPx())
      )
    }

    // Centered Album Art Disc
    Box(
      modifier = Modifier
        .fillMaxSize(0.44f)
        .rotate(currentRotation)
        .clip(CircleShape)
        .border(2.dp, NeonCyan, CircleShape)
        .background(MidnightBlack),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_default_album),
        contentDescription = "Album Artwork",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )

      // Spindle Center Hole
      Box(
        modifier = Modifier
          .size(16.dp)
          .clip(CircleShape)
          .background(MidnightBlack)
          .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
      )
    }
  }
}

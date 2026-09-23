package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqBand
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightCardSurface
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WaveformInactive

@Composable
fun EqBandSlider(
  band: EqBand,
  onLevelChanged: (Short) -> Unit,
  modifier: Modifier = Modifier
) {
  val level = band.levelMilliBels.toFloat()
  val min = band.minMilliBels.toFloat()
  val max = band.maxMilliBels.toFloat()

  Column(
    modifier = modifier
      .width(58.dp)
      .background(MidnightCardSurface, RoundedCornerShape(12.dp))
      .border(1.dp, MidnightElevated, RoundedCornerShape(12.dp))
      .padding(vertical = 12.dp, horizontal = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Gain readout
    val dbValue = band.dbGain
    val dbText = if (dbValue > 0) "+%.1fdB".format(dbValue) else "%.1fdB".format(dbValue)
    Text(
      text = dbText,
      color = if (dbValue > 0) NeonCyan else if (dbValue < 0) NeonPink else TextMuted,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold
    )

    // Vertical slider container
    Box(
      modifier = Modifier
        .height(140.dp)
        .width(48.dp),
      contentAlignment = Alignment.Center
    ) {
      Slider(
        value = level,
        onValueChange = { onLevelChanged(it.toInt().toShort()) },
        valueRange = min..max,
        modifier = Modifier
          .rotate(-90f)
          .width(130.dp)
          .testTag("eq_slider_${band.index}"),
        colors = SliderDefaults.colors(
          thumbColor = NeonCyan,
          activeTrackColor = NeonCyan,
          inactiveTrackColor = WaveformInactive
        )
      )
    }

    // Frequency Label
    Text(
      text = band.label,
      color = TextPrimary,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

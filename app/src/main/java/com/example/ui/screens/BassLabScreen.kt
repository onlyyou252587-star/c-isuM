package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EqBandSlider
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCardSurface
import com.example.ui.theme.MidnightDarkSurface
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WaveformInactive
import com.example.viewmodel.MusicViewModel

@Composable
fun BassLabScreen(
  viewModel: MusicViewModel,
  modifier: Modifier = Modifier
) {
  val effectSettings by viewModel.effectSettings.collectAsState()
  val scrollState = rememberScrollState()

  val presets = listOf(
    "Midnight Bass Signature",
    "Sub Overdrive",
    "Club & EDM",
    "Hip-Hop 808",
    "Rock Punch",
    "Vocal Clarity",
    "Flat"
  )

  val reverbOptions = listOf(
    Pair(0.toShort(), "None"),
    Pair(1.toShort(), "Small Room"),
    Pair(2.toShort(), "Medium Room"),
    Pair(3.toShort(), "Large Room"),
    Pair(4.toShort(), "Medium Hall"),
    Pair(5.toShort(), "Large Hall"),
    Pair(6.toShort(), "Plate")
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MidnightBlack)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .testTag("bass_lab_container")
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ElectricBolt,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "BASS LAB & DSP",
              color = TextPrimary,
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "Hardware Equalizer & Sub-Harmonic Audio Effects",
            color = TextMuted,
            fontSize = 12.sp
          )
        }

        OutlinedButton(
          onClick = { viewModel.applyPreset("Midnight Bass Signature") },
          modifier = Modifier.testTag("reset_eq_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Reset", fontSize = 12.sp, color = NeonCyan)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Midnight Sub-Bass Overdrive Banner / Switch
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCardSurface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.horizontalGradient(
            if (effectSettings.isMidnightSubBass)
              listOf(NeonCyan, NeonPink)
            else
              listOf(MidnightBorder, MidnightBorder)
          )
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("midnight_sub_card")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Midnight Sub-Bass Mode",
                color = if (effectSettings.isMidnightSubBass) NeonCyan else TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              if (effectSettings.isMidnightSubBass) {
                Box(
                  modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NeonPink)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "ACTIVE",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
            Text(
              text = "Sub-harmonic frequency boost at 40-60Hz with low-end saturation.",
              color = TextMuted,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          Switch(
            checked = effectSettings.isMidnightSubBass,
            onCheckedChange = { viewModel.toggleMidnightSubBass(it) },
            colors = SwitchDefaults.colors(
              checkedThumbColor = MidnightBlack,
              checkedTrackColor = NeonCyan,
              uncheckedThumbColor = TextMuted,
              uncheckedTrackColor = MidnightElevated
            ),
            modifier = Modifier.testTag("sub_bass_switch")
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Presets Selector Carousel
      Text(
        text = "EQUALIZER PRESETS",
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )

      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(presets) { preset ->
          val isSelected = effectSettings.currentPresetName == preset
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) NeonCyan else MidnightCardSurface)
              .border(
                1.dp,
                if (isSelected) NeonCyan else MidnightBorder,
                RoundedCornerShape(12.dp)
              )
              .clickable { viewModel.applyPreset(preset) }
              .padding(horizontal = 14.dp, vertical = 10.dp)
              .testTag("preset_$preset")
          ) {
            Text(
              text = preset,
              color = if (isSelected) MidnightBlack else TextPrimary,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 5-Band Graphic Equalizer
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightDarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(MidnightBorder, MidnightElevated))),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "5-Band Graphic Equalizer",
              color = TextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "±15 dB Range",
              color = NeonCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Band Sliders Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            effectSettings.eqBands.forEach { band ->
              EqBandSlider(
                band = band,
                onLevelChanged = { newLevel ->
                  viewModel.setEqBandLevel(band.index, newLevel)
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Bass Boost & 3D Virtualizer Knobs/Sliders
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Bass Boost Card
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MidnightCardSurface),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(MidnightBorder, MidnightBorder)))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Bass Boost",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${(effectSettings.bassBoostStrength / 10)}%",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
              value = effectSettings.bassBoostStrength.toFloat(),
              onValueChange = { viewModel.setBassBoost(it.toInt()) },
              valueRange = 0f..1000f,
              colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = WaveformInactive
              ),
              modifier = Modifier.testTag("bass_boost_slider")
            )
          }
        }

        // 3D Virtualizer Card
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MidnightCardSurface),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(MidnightBorder, MidnightBorder)))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "3D Surround",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${(effectSettings.virtualizerStrength / 10)}%",
                color = NeonViolet,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
              value = effectSettings.virtualizerStrength.toFloat(),
              onValueChange = { viewModel.setVirtualizer(it.toInt()) },
              valueRange = 0f..1000f,
              colors = SliderDefaults.colors(
                thumbColor = NeonViolet,
                activeTrackColor = NeonViolet,
                inactiveTrackColor = WaveformInactive
              ),
              modifier = Modifier.testTag("virtualizer_slider")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Environmental Reverb Selector
      Text(
        text = "ACOUSTIC REVERB",
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )

      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(reverbOptions) { (presetShort, name) ->
          val isSelected = effectSettings.reverbPreset == presetShort
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) NeonPink else MidnightCardSurface)
              .border(
                1.dp,
                if (isSelected) NeonPink else MidnightBorder,
                RoundedCornerShape(10.dp)
              )
              .clickable { viewModel.setReverb(presetShort, name) }
              .padding(horizontal = 12.dp, vertical = 8.dp)
              .testTag("reverb_$name")
          ) {
            Text(
              text = name,
              color = if (isSelected) Color.White else TextPrimary,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Playback Speed & Pitch Controls
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCardSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Speed
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Playback Speed (Tempo)",
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "%.2fx".format(effectSettings.playbackSpeed),
              color = NeonCyan,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Slider(
            value = effectSettings.playbackSpeed,
            onValueChange = { viewModel.setPlaybackSpeed(it) },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(
              thumbColor = NeonCyan,
              activeTrackColor = NeonCyan,
              inactiveTrackColor = WaveformInactive
            ),
            modifier = Modifier.testTag("speed_slider")
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Pitch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Audio Pitch (Key)",
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "%.2fx".format(effectSettings.playbackPitch),
              color = NeonOrange,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Slider(
            value = effectSettings.playbackPitch,
            onValueChange = { viewModel.setPlaybackPitch(it) },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(
              thumbColor = NeonOrange,
              activeTrackColor = NeonOrange,
              inactiveTrackColor = WaveformInactive
            ),
            modifier = Modifier.testTag("pitch_slider")
          )
        }
      }

      Spacer(modifier = Modifier.height(120.dp))
    }
  }
}

package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.components.EditTagDialog
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
fun AudioStudioScreen(
  viewModel: MusicViewModel,
  modifier: Modifier = Modifier
) {
  val allTracks by viewModel.allTracks.collectAsState()
  val targetTrack by viewModel.trimmerTargetTrack.collectAsState()
  val waveformPeaks by viewModel.waveformPeaks.collectAsState()
  val playbackState by viewModel.playbackState.collectAsState()

  val activeTrack = targetTrack ?: playbackState.currentTrack ?: allTracks.firstOrNull()

  LaunchedEffect(activeTrack) {
    if (activeTrack != null && targetTrack?.id != activeTrack.id) {
      viewModel.prepareTrimmerForTrack(activeTrack)
    }
  }

  val durationMs = activeTrack?.durationMs ?: 30_000L

  var startMs by remember(activeTrack) { mutableLongStateOf(0L) }
  var endMs by remember(activeTrack) { mutableLongStateOf((durationMs * 0.75f).toLong().coerceAtLeast(10_000L)) }
  var clipName by remember(activeTrack) { mutableStateOf("${activeTrack?.title ?: "Clip"} (Trimmed)") }
  var isPreviewingLoop by remember { mutableStateOf(false) }

  // Tag editor dialog state
  var editingTrack by remember { mutableStateOf<Track?>(null) }

  val scrollState = rememberScrollState()

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
        .testTag("audio_studio_container")
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
              imageVector = Icons.Default.ContentCut,
              contentDescription = null,
              tint = NeonPink,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "AUDIO STUDIO & TOOLS",
              color = TextPrimary,
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "Precision Trimmer, Ringtone Maker & DJ Presets",
            color = TextMuted,
            fontSize = 12.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Track Selector Row
      Text(
        text = "SELECT AUDIO TO EDIT",
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
        items(allTracks) { t ->
          val isSelected = activeTrack?.id == t.id
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) MidnightElevated else MidnightCardSurface)
              .border(
                1.dp,
                if (isSelected) NeonPink else MidnightBorder,
                RoundedCornerShape(12.dp)
              )
              .clickable { viewModel.prepareTrimmerForTrack(t) }
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Column {
              Text(
                text = t.title,
                color = if (isSelected) NeonPink else TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${t.formattedDuration} • ${t.artist}",
                color = TextMuted,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Audio Trimmer Waveform Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightDarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonPink, NeonPurple))),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Visual Waveform Trimmer",
              color = TextPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            val clipSec = ((endMs - startMs) / 1000f)
            Text(
              text = "Length: %.1fs".format(clipSec),
              color = NeonPink,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Waveform Canvas with Range Selection Highlights
          val peaks = if (waveformPeaks.isNotEmpty()) waveformPeaks else List(50) { 0.4f }
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(84.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MidnightBlack)
          ) {
            val w = size.width
            val h = size.height
            val barCount = peaks.size
            val spacing = 2.dp.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = ((w - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

            val startNorm = (startMs.toFloat() / durationMs).coerceIn(0f, 1f)
            val endNorm = (endMs.toFloat() / durationMs).coerceIn(0f, 1f)
            val startX = startNorm * w
            val endX = endNorm * w

            // Highlight trimmed range background
            drawRect(
              brush = Brush.horizontalGradient(
                listOf(NeonPink.copy(alpha = 0.2f), NeonPurple.copy(alpha = 0.2f))
              ),
              topLeft = Offset(startX, 0f),
              size = Size((endX - startX).coerceAtLeast(4f), h)
            )

            // Draw audio peaks
            peaks.forEachIndexed { index, peak ->
              val normIndex = index.toFloat() / barCount
              val barH = (peak * h * 0.85f).coerceIn(4.dp.toPx(), h)
              val x = index * (barWidth + spacing)
              val y = (h - barH) / 2f

              val isInRange = normIndex in startNorm..endNorm
              val color = if (isInRange) NeonCyan else WaveformInactive

              drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
              )
            }

            // Start & End markers
            drawLine(
              color = NeonPink,
              start = Offset(startX, 0f),
              end = Offset(startX, h),
              strokeWidth = 3.dp.toPx()
            )
            drawLine(
              color = NeonCyan,
              start = Offset(endX, 0f),
              end = Offset(endX, h),
              strokeWidth = 3.dp.toPx()
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Start & End Sliders with Time Readouts
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Start Marker: %02d:%02d".format((startMs / 1000) / 60, (startMs / 1000) % 60),
                color = NeonPink,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Row {
                Text(
                  text = "-1s",
                  color = TextMuted,
                  fontSize = 11.sp,
                  modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { startMs = (startMs - 1000).coerceAtLeast(0L) }
                )
                Text(
                  text = "+1s",
                  color = TextMuted,
                  fontSize = 11.sp,
                  modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { startMs = (startMs + 1000).coerceAtMost(endMs - 1000) }
                )
              }
            }

            Slider(
              value = startMs.toFloat(),
              onValueChange = { startMs = it.toLong().coerceAtMost(endMs - 1000L) },
              valueRange = 0f..durationMs.toFloat(),
              colors = SliderDefaults.colors(
                thumbColor = NeonPink,
                activeTrackColor = NeonPink,
                inactiveTrackColor = WaveformInactive
              ),
              modifier = Modifier.testTag("trim_start_slider")
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "End Marker: %02d:%02d".format((endMs / 1000) / 60, (endMs / 1000) % 60),
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Row {
                Text(
                  text = "-1s",
                  color = TextMuted,
                  fontSize = 11.sp,
                  modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { endMs = (endMs - 1000).coerceAtLeast(startMs + 1000) }
                )
                Text(
                  text = "+1s",
                  color = TextMuted,
                  fontSize = 11.sp,
                  modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { endMs = (endMs + 1000).coerceAtMost(durationMs) }
                )
              }
            }

            Slider(
              value = endMs.toFloat(),
              onValueChange = { endMs = it.toLong().coerceAtLeast(startMs + 1000L) },
              valueRange = 0f..durationMs.toFloat(),
              colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = WaveformInactive
              ),
              modifier = Modifier.testTag("trim_end_slider")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = clipName,
            onValueChange = { clipName = it },
            label = { Text("Exported Clip Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("clip_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonPink,
              unfocusedBorderColor = MidnightBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Preview & Export Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = {
                activeTrack?.let {
                  viewModel.audioEngine.playTrack(it, startMs)
                }
              },
              modifier = Modifier
                .weight(1f)
                .testTag("preview_selection_button")
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Preview Clip", color = NeonCyan)
            }

            Button(
              onClick = {
                viewModel.trimAndSaveAudio(startMs, endMs, clipName)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = NeonPink,
                contentColor = Color.White
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("export_clip_button")
            ) {
              Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save Clip", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Quick DJ Studio Audio FX Presets
      Text(
        text = "INSTANT DJ EFFECTS (TRANSFORM AUDIO)",
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Slowed + Reverb Preset
        Card(
          modifier = Modifier
            .weight(1f)
            .clickable {
              viewModel.setPlaybackSpeed(0.84f)
              viewModel.setReverb(5, "Large Hall")
              viewModel.setBassBoost(750)
              viewModel.showStatus("Applied: Slowed + Reverb")
            }
            .testTag("fx_slowed_reverb"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MidnightCardSurface),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonViolet, NeonPurple)))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Slowed + Reverb",
              color = NeonViolet,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "0.84x tempo • Large Hall • Deep Bass",
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }

        // Nightcore Preset
        Card(
          modifier = Modifier
            .weight(1f)
            .clickable {
              viewModel.setPlaybackSpeed(1.25f)
              viewModel.setPlaybackPitch(1.25f)
              viewModel.setReverb(0, "None")
              viewModel.showStatus("Applied: Nightcore")
            }
            .testTag("fx_nightcore"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MidnightCardSurface),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonOrange, NeonPink)))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Nightcore",
              color = NeonOrange,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "1.25x speed & pitch shift",
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Quick Metadata Editor Action
      activeTrack?.let { current ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MidnightCardSurface),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { editingTrack = current }
            .testTag("edit_active_tags_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Edit Tags for '${current.title}'",
                  color = TextPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "Update ID3 tags, artist, album, genre, year",
                  color = TextMuted,
                  fontSize = 11.sp
                )
              }
            }
            Text(
              text = "EDIT",
              color = NeonCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(120.dp))
    }
  }

  editingTrack?.let { tr ->
    EditTagDialog(
      track = tr,
      onDismiss = { editingTrack = null },
      onSave = { title, artist, album, genre, year ->
        viewModel.updateTrackMetadata(tr.id, title, artist, album, genre, year)
        editingTrack = null
      }
    )
  }
}

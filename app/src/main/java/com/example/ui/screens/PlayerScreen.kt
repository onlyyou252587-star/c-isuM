package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackState
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.VinylRecord
import com.example.ui.components.WaveformSeekBar
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCardSurface
import com.example.ui.theme.MidnightDarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MusicViewModel

/**
 * State container wrapper for the main Player Screen.
 */
@Composable
fun PlayerScreen(
  viewModel: MusicViewModel,
  onOpenBassLab: () -> Unit,
  modifier: Modifier = Modifier
) {
  val playbackState by viewModel.playbackState.collectAsState()
  val effectSettings by viewModel.effectSettings.collectAsState()

  MainPlayerLayout(
    playbackState = playbackState,
    presetName = effectSettings.currentPresetName,
    bassBoostStrength = effectSettings.bassBoostStrength,
    onPlayPauseClick = { viewModel.togglePlayPause() },
    onSkipNext = { viewModel.nextTrack() },
    onSkipPrevious = { viewModel.previousTrack() },
    onSeek = { progress -> viewModel.seekTo(progress) },
    onToggleFavorite = { track -> viewModel.toggleFavorite(track) },
    onToggleShuffle = { viewModel.toggleShuffle() },
    onCycleRepeatMode = { viewModel.cycleRepeatMode() },
    onOpenBassLab = onOpenBassLab,
    onSetSleepTimer = { minutes -> viewModel.setSleepTimer(minutes) },
    modifier = modifier
  )
}

/**
 * Base Compose layout for the main music player screen.
 * Contains:
 *  1. Track Display Area (Artwork, Title, Artist, Visualizer, Favorite)
 *  2. Seek Bar (Interactive Waveform scrubber with elapsed & remaining timestamps)
 *  3. Playback Controls (Play/Pause, Skip Next, Skip Previous, Shuffle, Repeat)
 */
@Composable
fun MainPlayerLayout(
  playbackState: PlaybackState,
  presetName: String,
  bassBoostStrength: Int,
  onPlayPauseClick: () -> Unit,
  onSkipNext: () -> Unit,
  onSkipPrevious: () -> Unit,
  onSeek: (Float) -> Unit,
  onToggleFavorite: (Track) -> Unit,
  onToggleShuffle: () -> Unit,
  onCycleRepeatMode: () -> Unit,
  onOpenBassLab: () -> Unit,
  onSetSleepTimer: (Int?) -> Unit,
  modifier: Modifier = Modifier
) {
  val track = playbackState.currentTrack
  var showSleepMenu by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            MidnightDarkSurface,
            MidnightBlack,
            Color(0xFF04070D)
          )
        )
      )
      .testTag("main_music_player_screen"),
    contentAlignment = Alignment.TopCenter
  ) {
    if (track == null) {
      EmptyPlayerLayout()
    } else {
      BoxWithConstraints(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 600.dp)
          .padding(horizontal = 20.dp, vertical = 10.dp)
      ) {
        val scrollState = rememberScrollState()

        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          // --- Top Header: Quick Access Bar ---
          PlayerTopBar(
            presetName = presetName,
            sleepTimerMinutes = playbackState.sleepTimerMinutesLeft,
            showSleepMenu = showSleepMenu,
            onOpenSleepMenu = { showSleepMenu = true },
            onDismissSleepMenu = { showSleepMenu = false },
            onOpenBassLab = onOpenBassLab,
            onSetSleepTimer = onSetSleepTimer
          )

          Spacer(modifier = Modifier.height(10.dp))

          // --- 1. TRACK DISPLAY AREA ---
          TrackDisplayArea(
            track = track,
            isPlaying = playbackState.isPlaying,
            bassBoostStrength = bassBoostStrength,
            visualizerAmplitudes = playbackState.visualizerAmplitudes,
            onToggleFavorite = { onToggleFavorite(track) }
          )

          Spacer(modifier = Modifier.height(14.dp))

          // --- 2. SEEK BAR ---
          PlayerSeekBar(
            progress = playbackState.progress,
            formattedPosition = playbackState.formattedPosition,
            formattedDuration = track.formattedDuration,
            onSeek = onSeek
          )

          Spacer(modifier = Modifier.height(14.dp))

          // --- 3. PLAYBACK CONTROLS ---
          PlaybackControlsRow(
            isPlaying = playbackState.isPlaying,
            isShuffleEnabled = playbackState.isShuffleEnabled,
            repeatMode = playbackState.repeatMode,
            onPlayPauseClick = onPlayPauseClick,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeatMode = onCycleRepeatMode
          )

          // Bottom spacing for padding above bottom bar
          Spacer(modifier = Modifier.height(96.dp))
        }
      }
    }
  }
}

/**
 * 1. Track Display Area: Album art / vinyl disc, live visualizer, track title & artist, favorite toggle.
 */
@Composable
fun TrackDisplayArea(
  track: Track,
  isPlaying: Boolean,
  bassBoostStrength: Int,
  visualizerAmplitudes: List<Float>,
  onToggleFavorite: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("track_display_area"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Dynamic Vinyl Record with pulsing bass glow
    VinylRecord(
      isPlaying = isPlaying,
      bassLevelFactor = (bassBoostStrength / 1000f),
      modifier = Modifier
        .fillMaxWidth(0.72f)
        .padding(vertical = 4.dp)
        .testTag("vinyl_record_artwork")
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Real-time Audio Spectrum Visualizer
    AudioVisualizer(
      amplitudes = visualizerAmplitudes,
      isPlaying = isPlaying,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
        .testTag("audio_visualizer")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Track Title, Artist, and Favorite heart action
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = track.title,
          color = TextPrimary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.testTag("track_title")
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Text(
            text = track.artist,
            color = TextSecondary,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("track_artist")
          )

          if (track.album.isNotBlank()) {
            Text(
              text = " • ${track.album}",
              color = TextMuted,
              fontSize = 12.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier
          .size(48.dp)
          .testTag("player_favorite_button")
      ) {
        Icon(
          imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = if (track.isFavorite) "Remove from favorites" else "Add to favorites",
          tint = if (track.isFavorite) NeonPink else TextMuted,
          modifier = Modifier.size(26.dp)
        )
      }
    }
  }
}

/**
 * 2. Seek Bar: Waveform scrubber with elapsed & remaining time indicators.
 */
@Composable
fun PlayerSeekBar(
  progress: Float,
  formattedPosition: String,
  formattedDuration: String,
  onSeek: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .testTag("seek_bar_container")
  ) {
    WaveformSeekBar(
      progress = progress,
      formattedPosition = formattedPosition,
      formattedDuration = formattedDuration,
      onSeek = onSeek
    )
  }
}

/**
 * 3. Playback Controls: Previous, Play/Pause, Next, Shuffle, and Repeat.
 */
@Composable
fun PlaybackControlsRow(
  isPlaying: Boolean,
  isShuffleEnabled: Boolean,
  repeatMode: RepeatMode,
  onPlayPauseClick: () -> Unit,
  onSkipNext: () -> Unit,
  onSkipPrevious: () -> Unit,
  onToggleShuffle: () -> Unit,
  onCycleRepeatMode: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag("playback_controls"),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Shuffle Button
    IconButton(
      onClick = onToggleShuffle,
      modifier = Modifier
        .size(48.dp)
        .testTag("shuffle_button")
    ) {
      Icon(
        imageVector = Icons.Default.Shuffle,
        contentDescription = if (isShuffleEnabled) "Disable shuffle" else "Enable shuffle",
        tint = if (isShuffleEnabled) NeonCyan else TextMuted,
        modifier = Modifier.size(22.dp)
      )
    }

    // Skip Previous Button (48dp+ interactive touch target)
    IconButton(
      onClick = onSkipPrevious,
      modifier = Modifier
        .size(52.dp)
        .testTag("skip_previous_button")
    ) {
      Icon(
        imageVector = Icons.Default.SkipPrevious,
        contentDescription = "Skip to previous track",
        tint = TextPrimary,
        modifier = Modifier.size(32.dp)
      )
    }

    // Play / Pause glowing elevated action button
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(CircleShape)
        .background(
          Brush.linearGradient(
            listOf(NeonCyan, Color(0xFF00B0FF))
          )
        )
        .border(2.dp, Color.White.copy(alpha = 0.45f), CircleShape)
        .clickable(onClick = onPlayPauseClick)
        .testTag("play_pause_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
        contentDescription = if (isPlaying) "Pause track" else "Play track",
        tint = MidnightBlack,
        modifier = Modifier.size(36.dp)
      )
    }

    // Skip Next Button (48dp+ interactive touch target)
    IconButton(
      onClick = onSkipNext,
      modifier = Modifier
        .size(52.dp)
        .testTag("skip_next_button")
    ) {
      Icon(
        imageVector = Icons.Default.SkipNext,
        contentDescription = "Skip to next track",
        tint = TextPrimary,
        modifier = Modifier.size(32.dp)
      )
    }

    // Repeat Mode Button
    IconButton(
      onClick = onCycleRepeatMode,
      modifier = Modifier
        .size(48.dp)
        .testTag("repeat_button")
    ) {
      val (icon, tint, desc) = when (repeatMode) {
        RepeatMode.ALL -> Triple(Icons.Default.Repeat, NeonCyan, "Repeat all tracks")
        RepeatMode.ONE -> Triple(Icons.Default.RepeatOne, NeonPink, "Repeat current track")
        RepeatMode.OFF -> Triple(Icons.Default.Repeat, TextMuted, "Repeat off")
      }
      Icon(
        imageVector = icon,
        contentDescription = desc,
        tint = tint,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}

/**
 * Top bar inside the player: DSP Preset badge and Sleep Timer dropdown.
 */
@Composable
private fun PlayerTopBar(
  presetName: String,
  sleepTimerMinutes: Int?,
  showSleepMenu: Boolean,
  onOpenSleepMenu: () -> Unit,
  onDismissSleepMenu: () -> Unit,
  onOpenBassLab: () -> Unit,
  onSetSleepTimer: (Int?) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Preset Chip
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(MidnightCardSurface)
        .border(1.dp, MidnightBorder, RoundedCornerShape(20.dp))
        .clickable(onClick = onOpenBassLab)
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .testTag("preset_chip")
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Equalizer,
          contentDescription = null,
          tint = NeonCyan,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = presetName,
          color = NeonCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // Sleep Timer Menu
    Box {
      IconButton(
        onClick = onOpenSleepMenu,
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(MidnightCardSurface)
          .border(1.dp, MidnightBorder, CircleShape)
          .testTag("sleep_timer_button")
      ) {
        Icon(
          imageVector = Icons.Default.Bedtime,
          contentDescription = "Sleep Timer",
          tint = if (sleepTimerMinutes != null) NeonViolet else TextMuted,
          modifier = Modifier.size(18.dp)
        )
      }

      DropdownMenu(
        expanded = showSleepMenu,
        onDismissRequest = onDismissSleepMenu
      ) {
        DropdownMenuItem(
          text = { Text("Turn Off Sleep Timer") },
          onClick = {
            onSetSleepTimer(null)
            onDismissSleepMenu()
          }
        )
        DropdownMenuItem(
          text = { Text("15 Minutes") },
          onClick = {
            onSetSleepTimer(15)
            onDismissSleepMenu()
          }
        )
        DropdownMenuItem(
          text = { Text("30 Minutes") },
          onClick = {
            onSetSleepTimer(30)
            onDismissSleepMenu()
          }
        )
        DropdownMenuItem(
          text = { Text("45 Minutes") },
          onClick = {
            onSetSleepTimer(45)
            onDismissSleepMenu()
          }
        )
        DropdownMenuItem(
          text = { Text("60 Minutes") },
          onClick = {
            onSetSleepTimer(60)
            onDismissSleepMenu()
          }
        )
      }
    }
  }
}

/**
 * Empty state when no audio track is loaded.
 */
@Composable
private fun EmptyPlayerLayout() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.GraphicEq,
      contentDescription = null,
      tint = NeonCyan,
      modifier = Modifier.size(64.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "Audio Vault Idle",
      color = TextPrimary,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Select a track from the library to initiate playback with real-time bass effects.",
      color = TextMuted,
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 8.dp)
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF080C14)
@Composable
private fun MainPlayerLayoutPreview() {
  val sampleTrack = Track(
    id = 1L,
    title = "Cybernetic Pulse 808",
    artist = "Midnight Bass Engine",
    album = "Sub-Frequency Vol. 1",
    durationMs = 184_000L,
    uri = "",
    isFavorite = true
  )

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MidnightBlack
  ) {
    MainPlayerLayout(
      playbackState = PlaybackState(
        currentTrack = sampleTrack,
        isPlaying = true,
        currentPositionMs = 62_000L,
        durationMs = 184_000L,
        isShuffleEnabled = true,
        repeatMode = RepeatMode.ALL,
        visualizerAmplitudes = List(20) { (it % 5 + 1) * 0.18f }
      ),
      presetName = "Midnight Bass Signature",
      bassBoostStrength = 800,
      onPlayPauseClick = {},
      onSkipNext = {},
      onSkipPrevious = {},
      onSeek = {},
      onToggleFavorite = {},
      onToggleShuffle = {},
      onCycleRepeatMode = {},
      onOpenBassLab = {},
      onSetSleepTimer = {}
    )
  }
}

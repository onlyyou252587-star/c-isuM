package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PlaybackState
import com.example.model.Track
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCardSurface
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun MiniPlayer(
  playbackState: PlaybackState,
  onPlayPauseClick: () -> Unit,
  onNextClick: () -> Unit,
  onFavoriteClick: (Track) -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val track = playbackState.currentTrack ?: return

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(MidnightCardSurface)
      .border(1.dp, MidnightBorder, RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("mini_player")
  ) {
    // Mini linear progress indicator at the top
    LinearProgressIndicator(
      progress = { playbackState.progress },
      modifier = Modifier
        .fillMaxWidth()
        .height(2.dp),
      color = NeonCyan,
      trackColor = MidnightElevated,
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Album art thumbnail
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(MidnightBlack)
          .border(1.dp, MidnightElevated, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_default_album),
          contentDescription = "Track Artwork",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      }

      // Title & Artist
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 12.dp)
      ) {
        Text(
          text = track.title,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = track.artist,
          color = TextMuted,
          fontSize = 12.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Controls
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = { onFavoriteClick(track) },
          modifier = Modifier
            .size(36.dp)
            .testTag("mini_favorite_button")
        ) {
          Icon(
            imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (track.isFavorite) NeonPink else TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }

        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(NeonCyan)
            .clickable(onClick = onPlayPauseClick)
            .testTag("mini_play_pause_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
            tint = MidnightBlack,
            modifier = Modifier.size(22.dp)
          )
        }

        IconButton(
          onClick = onNextClick,
          modifier = Modifier
            .size(36.dp)
            .testTag("mini_next_button")
        ) {
          Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Next Track",
            tint = TextPrimary,
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }
  }
}

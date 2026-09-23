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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlaylistEntity
import com.example.model.Track
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCardSurface
import com.example.ui.theme.MidnightDarkSurface
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MusicViewModel

@Composable
fun PlaylistsScreen(
  viewModel: MusicViewModel,
  modifier: Modifier = Modifier
) {
  val playlists by viewModel.playlists.collectAsState()
  var showCreateDialog by remember { mutableStateOf(false) }
  var selectedPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MidnightBlack)
  ) {
    if (selectedPlaylist == null) {
      // Main Playlists Overview
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 12.dp)
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
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                tint = NeonViolet,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "PLAYLISTS",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
              )
            }
            Text(
              text = "Custom Bass Mixes & Collections",
              color = TextMuted,
              fontSize = 12.sp
            )
          }

          Button(
            onClick = { showCreateDialog = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = NeonViolet,
              contentColor = MidnightBlack
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("create_playlist_fab")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("New", fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (playlists.isEmpty()) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.QueueMusic,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No playlists yet",
              color = TextPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Create custom collections for your late-night drives or workout sessions.",
              color = TextMuted,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(playlists) { playlist ->
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedPlaylist = playlist }
                  .testTag("playlist_item_${playlist.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightCardSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                  brush = Brush.horizontalGradient(listOf(MidnightBorder, MidnightElevated))
                )
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(48.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(
                        Brush.linearGradient(listOf(NeonViolet, NeonPurple))
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.QueueMusic,
                      contentDescription = null,
                      tint = MidnightBlack,
                      modifier = Modifier.size(24.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(14.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = playlist.name,
                      color = TextPrimary,
                      fontSize = 16.sp,
                      fontWeight = FontWeight.Bold
                    )
                    if (playlist.description.isNotBlank()) {
                      Text(
                        text = playlist.description,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }

                  IconButton(
                    onClick = { viewModel.deletePlaylist(playlist.id) },
                    modifier = Modifier.size(40.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete Playlist",
                      tint = TextMuted,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }

            item {
              Spacer(modifier = Modifier.height(120.dp))
            }
          }
        }
      }
    } else {
      // Playlist Detail View
      PlaylistDetailView(
        playlist = selectedPlaylist!!,
        viewModel = viewModel,
        onBack = { selectedPlaylist = null }
      )
    }

    if (showCreateDialog) {
      CreatePlaylistDialog(
        onDismiss = { showCreateDialog = false },
        onCreate = { name, desc ->
          viewModel.createPlaylist(name, desc)
          showCreateDialog = false
        }
      )
    }
  }
}

@Composable
fun PlaylistDetailView(
  playlist: PlaylistEntity,
  viewModel: MusicViewModel,
  onBack: () -> Unit
) {
  val tracks by viewModel.getTracksForPlaylist(playlist.id).collectAsState(initial = emptyList())

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = TextPrimary
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = playlist.name,
          color = TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${tracks.size} tracks",
          color = TextMuted,
          fontSize = 12.sp
        )
      }

      if (tracks.isNotEmpty()) {
        Button(
          onClick = { viewModel.playTrackFromList(tracks, 0) },
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonCyan,
            contentColor = MidnightBlack
          ),
          shape = CircleShape,
          modifier = Modifier.testTag("play_playlist_button")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Play All", fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (tracks.isEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Playlist is empty",
          color = TextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Go to the Library screen and tap 'Add to Playlist' on any track.",
          color = TextMuted,
          fontSize = 13.sp,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        itemsIndexed(tracks) { index, track ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.playTrackFromList(tracks, index) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCardSurface)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${index + 1}",
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.width(24.dp)
              )

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = track.title,
                  color = TextPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "${track.artist} • ${track.formattedDuration}",
                  color = TextMuted,
                  fontSize = 11.sp
                )
              }

              IconButton(
                onClick = { viewModel.removeTrackFromPlaylist(playlist.id, track.id) },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Remove",
                  tint = TextMuted,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(120.dp))
        }
      }
    }
  }
}

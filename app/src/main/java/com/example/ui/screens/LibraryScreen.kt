package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Track
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.EditTagDialog
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCardSurface
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MusicViewModel
import com.example.viewmodel.SortOrder

@Composable
fun LibraryScreen(
  viewModel: MusicViewModel,
  onNavigateToStudio: (Track) -> Unit,
  modifier: Modifier = Modifier
) {
  val allTracks by viewModel.allTracks.collectAsState()
  val favoriteTracks by viewModel.favoriteTracks.collectAsState()
  val playlists by viewModel.playlists.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val sortOrder by viewModel.sortOrder.collectAsState()
  val playbackState by viewModel.playbackState.collectAsState()

  var selectedFilter by remember { mutableStateOf("All") } // "All", "Favorites", "Demos"
  var showSortMenu by remember { mutableStateOf(false) }

  // Dialog states
  var editingTrack by remember { mutableStateOf<Track?>(null) }
  var trackToAddToPlaylist by remember { mutableStateOf<Track?>(null) }
  var showCreatePlaylistDialog by remember { mutableStateOf(false) }

  // File picker launcher
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenMultipleDocuments()
  ) { uris: List<Uri> ->
    uris.forEach { uri ->
      viewModel.importAudioUri(uri)
    }
  }

  // Permission launcher for scanning device storage
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    viewModel.scanDeviceStorage()
  }

  // Filter & Sort list
  val filteredTracks = remember(allTracks, favoriteTracks, selectedFilter, searchQuery, sortOrder) {
    val baseList = when (selectedFilter) {
      "Favorites" -> favoriteTracks
      "Demos" -> allTracks.filter { it.isDemo }
      else -> allTracks
    }

    val searched = if (searchQuery.isBlank()) {
      baseList
    } else {
      baseList.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
      }
    }

    when (sortOrder) {
      SortOrder.TITLE_ASC -> searched.sortedBy { it.title.lowercase() }
      SortOrder.ARTIST_ASC -> searched.sortedBy { it.artist.lowercase() }
      SortOrder.DURATION_DESC -> searched.sortedByDescending { it.durationMs }
      SortOrder.DATE_DESC -> searched.sortedByDescending { it.dateAdded }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MidnightBlack)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("library_list")
    ) {
      // Hero Visual Header
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_midnight_hero),
            contentDescription = "Midnight Studio Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
          // Gradient Scrim
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Transparent,
                    MidnightBlack.copy(alpha = 0.6f),
                    MidnightBlack
                  )
                )
              )
          )

          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(horizontal = 16.dp, vertical = 10.dp)
          ) {
            Text(
              text = "MIDNIGHT BASS",
              color = NeonCyan,
              fontSize = 22.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 2.sp
            )
            Text(
              text = "${allTracks.size} Tracks in Audio Vault • Real-time DSP",
              color = TextSecondary,
              fontSize = 12.sp
            )
          }
        }
      }

      // Search & Sort Bar
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { viewModel.setSearchQuery(it) },
              modifier = Modifier
                .weight(1f)
                .testTag("search_input"),
              placeholder = { Text("Search songs, artists, albums...", fontSize = 13.sp, color = TextMuted) },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  tint = NeonCyan
                )
              },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { viewModel.setSearchQuery("") }) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Clear search",
                      tint = TextMuted
                    )
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MidnightBorder,
                focusedContainerColor = MidnightCardSurface,
                unfocusedContainerColor = MidnightCardSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box {
              IconButton(
                onClick = { showSortMenu = true },
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(MidnightCardSurface)
                  .border(1.dp, MidnightBorder, RoundedCornerShape(14.dp))
                  .testTag("sort_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Sort,
                  contentDescription = "Sort Options",
                  tint = NeonCyan
                )
              }

              DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
              ) {
                DropdownMenuItem(
                  text = { Text("Title (A-Z)") },
                  onClick = {
                    viewModel.setSortOrder(SortOrder.TITLE_ASC)
                    showSortMenu = false
                  }
                )
                DropdownMenuItem(
                  text = { Text("Artist (A-Z)") },
                  onClick = {
                    viewModel.setSortOrder(SortOrder.ARTIST_ASC)
                    showSortMenu = false
                  }
                )
                DropdownMenuItem(
                  text = { Text("Duration (Longest)") },
                  onClick = {
                    viewModel.setSortOrder(SortOrder.DURATION_DESC)
                    showSortMenu = false
                  }
                )
                DropdownMenuItem(
                  text = { Text("Date Added") },
                  onClick = {
                    viewModel.setSortOrder(SortOrder.DATE_DESC)
                    showSortMenu = false
                  }
                )
              }
            }
          }
        }
      }

      // Quick Action Chips: Filter / Import / Scan
      item {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            FilterChip(
              selected = selectedFilter == "All",
              onClick = { selectedFilter = "All" },
              label = { Text("All Tracks (${allTracks.size})") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NeonCyan,
                selectedLabelColor = MidnightBlack,
                containerColor = MidnightCardSurface,
                labelColor = TextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = MidnightBorder,
                selectedBorderColor = NeonCyan,
                enabled = true,
                selected = selectedFilter == "All"
              )
            )
          }

          item {
            FilterChip(
              selected = selectedFilter == "Favorites",
              onClick = { selectedFilter = "Favorites" },
              label = { Text("★ Favorites (${favoriteTracks.size})") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NeonPink,
                selectedLabelColor = MidnightBlack,
                containerColor = MidnightCardSurface,
                labelColor = TextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = MidnightBorder,
                selectedBorderColor = NeonPink,
                enabled = true,
                selected = selectedFilter == "Favorites"
              )
            )
          }

          item {
            FilterChip(
              selected = false,
              onClick = { filePickerLauncher.launch(arrayOf("audio/*")) },
              label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = NeonViolet
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Import File", color = NeonViolet, fontWeight = FontWeight.SemiBold)
                }
              },
              colors = FilterChipDefaults.filterChipColors(
                containerColor = MidnightCardSurface
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = NeonViolet.copy(alpha = 0.5f),
                enabled = true,
                selected = false
              ),
              modifier = Modifier.testTag("action_import_file")
            )
          }

          item {
            FilterChip(
              selected = false,
              onClick = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                  permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                  permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
              },
              label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = NeonCyan
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Scan Storage", color = NeonCyan)
                }
              },
              colors = FilterChipDefaults.filterChipColors(
                containerColor = MidnightCardSurface
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = MidnightBorder,
                enabled = true,
                selected = false
              ),
              modifier = Modifier.testTag("action_scan_storage")
            )
          }
        }
      }

      // Empty State
      if (filteredTracks.isEmpty()) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Audiotrack,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No tracks found",
              color = TextPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Import local audio files or scan device storage to expand your library.",
              color = TextMuted,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      } else {
        // Track list items
        itemsIndexed(
          items = filteredTracks,
          key = { _, track -> track.id }
        ) { index, track ->
          val isCurrentlyPlaying = playbackState.currentTrack?.id == track.id
          TrackListItem(
            track = track,
            isPlaying = isCurrentlyPlaying && playbackState.isPlaying,
            isCurrent = isCurrentlyPlaying,
            onClick = {
              viewModel.playTrackFromList(filteredTracks, index)
            },
            onFavoriteClick = { viewModel.toggleFavorite(track) },
            onAddToPlaylist = { trackToAddToPlaylist = track },
            onTrimClick = { onNavigateToStudio(track) },
            onEditTags = { editingTrack = track },
            onDelete = { viewModel.deleteTrack(track) }
          )
        }
      }

      // Bottom Spacer for mini-player padding
      item {
        Spacer(modifier = Modifier.height(120.dp))
      }
    }
  }

  // Edit Tag Dialog
  editingTrack?.let { track ->
    EditTagDialog(
      track = track,
      onDismiss = { editingTrack = null },
      onSave = { title, artist, album, genre, year ->
        viewModel.updateTrackMetadata(track.id, title, artist, album, genre, year)
        editingTrack = null
      }
    )
  }

  // Add to Playlist Dialog
  trackToAddToPlaylist?.let { track ->
    AddToPlaylistDialog(
      playlists = playlists,
      onDismiss = { trackToAddToPlaylist = null },
      onSelectPlaylist = { playlistId ->
        viewModel.addTrackToPlaylist(playlistId, track.id)
        trackToAddToPlaylist = null
      },
      onCreateNewPlaylist = {
        showCreatePlaylistDialog = true
      }
    )
  }

  // Create Playlist Dialog
  if (showCreatePlaylistDialog) {
    CreatePlaylistDialog(
      onDismiss = { showCreatePlaylistDialog = false },
      onCreate = { name, desc ->
        viewModel.createPlaylist(name, desc)
        showCreatePlaylistDialog = false
      }
    )
  }
}

@Composable
fun TrackListItem(
  track: Track,
  isPlaying: Boolean,
  isCurrent: Boolean,
  onClick: () -> Unit,
  onFavoriteClick: () -> Unit,
  onAddToPlaylist: () -> Unit,
  onTrimClick: () -> Unit,
  onEditTags: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showMenu by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp)
      .testTag("track_item_${track.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCurrent) MidnightElevated else MidnightCardSurface
    ),
    border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPurple))) else null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Thumbnail
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MidnightBlack)
          .border(1.dp, if (isCurrent) NeonCyan else MidnightBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_default_album),
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        if (isPlaying) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MidnightBlack.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Playing",
              tint = NeonCyan,
              modifier = Modifier.size(26.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Track Info
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = track.title,
          color = if (isCurrent) NeonCyan else TextPrimary,
          fontSize = 15.sp,
          fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Text(
            text = track.artist,
            color = TextMuted,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )

          Text(
            text = " • ${track.formattedDuration}",
            color = TextMuted,
            fontSize = 12.sp
          )

          if (track.isDemo) {
            Box(
              modifier = Modifier
                .padding(start = 6.dp)
                .background(NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = "DSP DEMO",
                color = NeonViolet,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Favorite toggle
      IconButton(
        onClick = onFavoriteClick,
        modifier = Modifier.size(40.dp)
      ) {
        Icon(
          imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = "Favorite",
          tint = if (track.isFavorite) NeonPink else TextMuted,
          modifier = Modifier.size(20.dp)
        )
      }

      // Overflow Menu
      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Track Actions",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Play Now") },
            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan) },
            onClick = {
              onClick()
              showMenu = false
            }
          )
          DropdownMenuItem(
            text = { Text("Add to Playlist") },
            leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = NeonViolet) },
            onClick = {
              onAddToPlaylist()
              showMenu = false
            }
          )
          DropdownMenuItem(
            text = { Text("Trim / Ringtone Studio") },
            leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null, tint = NeonPink) },
            onClick = {
              onTrimClick()
              showMenu = false
            }
          )
          DropdownMenuItem(
            text = { Text("Edit Metadata Tags") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary) },
            onClick = {
              onEditTags()
              showMenu = false
            }
          )
          DropdownMenuItem(
            text = { Text("Delete Track") },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
            onClick = {
              onDelete()
              showMenu = false
            }
          )
        }
      }
    }
  }
}

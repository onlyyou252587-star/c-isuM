package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.AudioStudioScreen
import com.example.ui.screens.BassLabScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightCardSurface
import com.example.ui.theme.MidnightDarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: MusicViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        MidnightBassApp(viewModel)
      }
    }
  }
}

@Composable
fun MidnightBassApp(viewModel: MusicViewModel) {
  val selectedTab by viewModel.selectedTab.collectAsState()
  val playbackState by viewModel.playbackState.collectAsState()
  val statusMessage by viewModel.statusMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(statusMessage) {
    statusMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatus()
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(MidnightBlack),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      Column(
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.navigationBars)
          .background(MidnightDarkSurface)
      ) {
        // Show MiniPlayer if a track is selected AND not currently in the full Player tab
        AnimatedVisibility(
          visible = playbackState.currentTrack != null && selectedTab != 1,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          MiniPlayer(
            playbackState = playbackState,
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onNextClick = { viewModel.nextTrack() },
            onFavoriteClick = { track -> viewModel.toggleFavorite(track) },
            onClick = { viewModel.setTab(1) } // Navigate to Player
          )
        }

        NavigationBar(
          containerColor = MidnightDarkSurface,
          modifier = Modifier.testTag("bottom_navigation_bar")
        ) {
          NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { viewModel.setTab(0) },
            icon = {
              Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = stringResource(R.string.nav_library)
              )
            },
            label = {
              Text(
                text = stringResource(R.string.nav_library),
                fontSize = 11.sp,
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MidnightBlack,
              selectedTextColor = NeonCyan,
              indicatorColor = NeonCyan,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_library")
          )

          NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { viewModel.setTab(1) },
            icon = {
              Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = stringResource(R.string.nav_player)
              )
            },
            label = {
              Text(
                text = stringResource(R.string.nav_player),
                fontSize = 11.sp,
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MidnightBlack,
              selectedTextColor = NeonCyan,
              indicatorColor = NeonCyan,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_player")
          )

          NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { viewModel.setTab(2) },
            icon = {
              Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = stringResource(R.string.nav_effects)
              )
            },
            label = {
              Text(
                text = stringResource(R.string.nav_effects),
                fontSize = 11.sp,
                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MidnightBlack,
              selectedTextColor = NeonCyan,
              indicatorColor = NeonCyan,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_effects")
          )

          NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { viewModel.setTab(3) },
            icon = {
              Icon(
                imageVector = Icons.Default.ContentCut,
                contentDescription = stringResource(R.string.nav_editor)
              )
            },
            label = {
              Text(
                text = stringResource(R.string.nav_editor),
                fontSize = 11.sp,
                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MidnightBlack,
              selectedTextColor = NeonCyan,
              indicatorColor = NeonCyan,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_studio")
          )

          NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { viewModel.setTab(4) },
            icon = {
              Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = stringResource(R.string.nav_playlists)
              )
            },
            label = {
              Text(
                text = stringResource(R.string.nav_playlists),
                fontSize = 11.sp,
                fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MidnightBlack,
              selectedTextColor = NeonCyan,
              indicatorColor = NeonCyan,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_playlists")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MidnightBlack)
    ) {
      when (selectedTab) {
        0 -> LibraryScreen(
          viewModel = viewModel,
          onNavigateToStudio = { track ->
            viewModel.prepareTrimmerForTrack(track)
            viewModel.setTab(3) // Jump to Audio Studio tab
          }
        )
        1 -> PlayerScreen(
          viewModel = viewModel,
          onOpenBassLab = { viewModel.setTab(2) }
        )
        2 -> BassLabScreen(viewModel = viewModel)
        3 -> AudioStudioScreen(viewModel = viewModel)
        4 -> PlaylistsScreen(viewModel = viewModel)
      }
    }
  }
}

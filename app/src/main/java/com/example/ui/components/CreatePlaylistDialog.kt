package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.PlaylistEntity
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightDarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun CreatePlaylistDialog(
  onDismiss: () -> Unit,
  onCreate: (name: String, description: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MidnightDarkSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MidnightBorder, RoundedCornerShape(20.dp))
        .testTag("create_playlist_dialog")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "New Playlist",
          color = NeonCyan,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Curate your midnight bass vibes",
          color = TextMuted,
          fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Playlist Name") },
          placeholder = { Text("e.g. Late Night Drives") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("playlist_name_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = MidnightBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description (Optional)") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = MidnightBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(
            onClick = onDismiss,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder))
          ) {
            Text("Cancel")
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              if (name.isNotBlank()) onCreate(name, description)
            },
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
              containerColor = NeonCyan,
              contentColor = MidnightBlack
            ),
            modifier = Modifier
              .padding(start = 8.dp)
              .testTag("submit_create_playlist_button")
          ) {
            Text("Create", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun AddToPlaylistDialog(
  playlists: List<PlaylistEntity>,
  onDismiss: () -> Unit,
  onSelectPlaylist: (playlistId: Long) -> Unit,
  onCreateNewPlaylist: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MidnightDarkSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MidnightBorder, RoundedCornerShape(20.dp))
        .testTag("add_to_playlist_dialog")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Add to Playlist",
          color = NeonCyan,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (playlists.isEmpty()) {
          Text(
            text = "No playlists found. Create one first!",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 12.dp)
          )
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height((playlists.size * 52).coerceAtMost(220).dp)
          ) {
            items(playlists) { pl ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onSelectPlaylist(pl.id) }
                  .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.QueueMusic,
                  contentDescription = null,
                  tint = NeonViolet,
                  modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                  text = pl.name,
                  color = TextPrimary,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Button(
            onClick = onCreateNewPlaylist,
            colors = ButtonDefaults.buttonColors(
              containerColor = NeonViolet,
              contentColor = MidnightBlack
            )
          ) {
            Text("+ New Playlist", fontWeight = FontWeight.SemiBold)
          }

          OutlinedButton(
            onClick = onDismiss,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
          ) {
            Text("Close")
          }
        }
      }
    }
  }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Track
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCardSurface
import com.example.ui.theme.MidnightDarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun EditTagDialog(
  track: Track,
  onDismiss: () -> Unit,
  onSave: (title: String, artist: String, album: String, genre: String?, year: Int?) -> Unit
) {
  var title by remember { mutableStateOf(track.title) }
  var artist by remember { mutableStateOf(track.artist) }
  var album by remember { mutableStateOf(track.album) }
  var genre by remember { mutableStateOf(track.genre ?: "") }
  var yearStr by remember { mutableStateOf(track.year?.toString() ?: "") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MidnightDarkSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MidnightBorder, RoundedCornerShape(20.dp))
        .testTag("edit_tags_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        Text(
          text = "Edit Audio Metadata",
          color = NeonCyan,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Modify ID3 tags & library info for this track",
          color = TextMuted,
          fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("tag_title_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = MidnightBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = artist,
          onValueChange = { artist = it },
          label = { Text("Artist") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("tag_artist_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = MidnightBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = album,
          onValueChange = { album = it },
          label = { Text("Album") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = MidnightBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = genre,
            onValueChange = { genre = it },
            label = { Text("Genre") },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = MidnightBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            singleLine = true
          )

          OutlinedTextField(
            value = yearStr,
            onValueChange = { yearStr = it },
            label = { Text("Year") },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = MidnightBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            singleLine = true
          )
        }

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
              val year = yearStr.toIntOrNull()
              onSave(title, artist, album, genre.ifBlank { null }, year)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = NeonCyan,
              contentColor = MidnightBlack
            ),
            modifier = Modifier
              .padding(start = 8.dp)
              .testTag("save_tags_button")
          ) {
            Text("Save Tags", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

package com.example.audio

import android.content.Context
import com.example.data.local.TrackEntity
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object AudioSynthesizer {

  fun ensureSampleTracks(context: Context): List<TrackEntity> {
    val dir = File(context.filesDir, "midnight_samples")
    if (!dir.exists()) {
      dir.mkdirs()
    }

    val tracks = mutableListOf<TrackEntity>()

    // Track 1: Midnight Sub Pulse
    val file1 = File(dir, "midnight_sub_pulse.wav")
    if (!file1.exists() || file1.length() < 1000) {
      generateSynthwaveWav(file1, baseBpm = 124, subFreq = 48.0, durationSec = 35)
    }
    tracks.add(
      TrackEntity(
        id = 1,
        title = "Midnight Sub Pulse",
        artist = "Midnight Bass Labs",
        album = "Sub-Harmonic Sessions Vol. 1",
        durationMs = 35_000,
        uri = file1.absolutePath,
        genre = "Future Bass / 808",
        year = 2026,
        isFavorite = true,
        isDemo = true,
        customBpm = 124
      )
    )

    // Track 2: Neon Cyber Groove
    val file2 = File(dir, "neon_cyber_groove.wav")
    if (!file2.exists() || file2.length() < 1000) {
      generateCyberpunkWav(file2, baseBpm = 130, durationSec = 32)
    }
    tracks.add(
      TrackEntity(
        id = 2,
        title = "Neon Cyber Groove",
        artist = "Cyber Pulse",
        album = "Obsidian Nights",
        durationMs = 32_000,
        uri = file2.absolutePath,
        genre = "Synthwave / Cyberpunk",
        year = 2026,
        isFavorite = false,
        isDemo = true,
        customBpm = 130
      )
    )

    // Track 3: Lo-Fi Midnight Reverie
    val file3 = File(dir, "lofi_midnight_reverie.wav")
    if (!file3.exists() || file3.length() < 1000) {
      generateLoFiWav(file3, baseBpm = 86, durationSec = 38)
    }
    tracks.add(
      TrackEntity(
        id = 3,
        title = "Lo-Fi Midnight Reverie",
        artist = "Velvet Resonance",
        album = "Late Night Acoustics",
        durationMs = 38_000,
        uri = file3.absolutePath,
        genre = "Chillhop / Deep Bass",
        year = 2026,
        isFavorite = true,
        isDemo = true,
        customBpm = 86
      )
    )

    // Track 4: 808 Seismic Impact
    val file4 = File(dir, "808_seismic_impact.wav")
    if (!file4.exists() || file4.length() < 1000) {
      generateTrap808Wav(file4, baseBpm = 140, durationSec = 30)
    }
    tracks.add(
      TrackEntity(
        id = 4,
        title = "808 Seismic Impact",
        artist = "Sub Richter",
        album = "Low Frequency Oscillations",
        durationMs = 30_000,
        uri = file4.absolutePath,
        genre = "Trap / Heavy Bass",
        year = 2026,
        isFavorite = false,
        isDemo = true,
        customBpm = 140
      )
    )

    return tracks
  }

  // Generate 44.1kHz 16-bit stereo PCM WAV with real beats, bassline & harmonics
  private fun generateSynthwaveWav(file: File, baseBpm: Int, subFreq: Double, durationSec: Int) {
    val sampleRate = 44100
    val totalSamples = sampleRate * durationSec
    val beatSamples = (sampleRate * 60.0 / baseBpm).toInt()

    val pcmData = ShortArray(totalSamples * 2) // Stereo (L, R)

    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val beatIndex = (i / beatSamples) % 16
      val beatPos = (i % beatSamples).toDouble() / beatSamples

      // 1. Kick Drum (on beat 0, 4, 8, 12)
      var kick = 0.0
      if (beatIndex % 4 == 0) {
        val kickT = beatPos * (60.0 / baseBpm)
        val kickPitch = 120.0 * exp(-kickT * 22.0) + subFreq
        kick = sin(2.0 * PI * kickPitch * kickT) * exp(-kickT * 10.0) * 0.85
      }

      // 2. Rolling Bassline (16th notes pitch changing)
      val noteIndex = (i / (beatSamples / 4)) % 16
      val noteFrequencies = doubleArrayOf(
        subFreq, subFreq, subFreq * 1.122, subFreq,
        subFreq * 0.89, subFreq * 0.89, subFreq, subFreq * 1.334,
        subFreq, subFreq, subFreq * 1.122, subFreq,
        subFreq * 1.498, subFreq * 1.334, subFreq * 1.122, subFreq * 0.89
      )
      val currentBassFreq = noteFrequencies[noteIndex]
      val subT = (i % (beatSamples / 4)).toDouble() / sampleRate
      val bassEnvelope = exp(-subT * 4.5)
      // Fundamental + 2nd harmonic + 3rd harmonic for rich saturation
      val bass = (sin(2.0 * PI * currentBassFreq * t) * 0.7 +
          sin(2.0 * PI * currentBassFreq * 2.0 * t) * 0.25 +
          sin(2.0 * PI * currentBassFreq * 3.0 * t) * 0.1) * bassEnvelope * 0.7

      // 3. Snare / Clap (beat 4, 12)
      var snare = 0.0
      if (beatIndex == 4 || beatIndex == 12) {
        val snareT = beatPos * (60.0 / baseBpm)
        val noise = (Math.random() * 2.0 - 1.0) * exp(-snareT * 14.0)
        val tone = sin(2.0 * PI * 180.0 * snareT) * exp(-snareT * 18.0) * 0.4
        snare = (noise + tone) * 0.6
      }

      // 4. Hi-hats (every 8th note)
      var hihat = 0.0
      val hatStep = (i / (beatSamples / 2)) % 2
      val hatT = ((i % (beatSamples / 2)).toDouble() / sampleRate)
      val hatNoise = (Math.random() * 2.0 - 1.0) * exp(-hatT * 60.0)
      hihat = hatNoise * (if (hatStep == 1) 0.3 else 0.15)

      // 5. Synth pad chords
      val chordFreqs = doubleArrayOf(220.0, 261.63, 329.63, 392.0)
      var pad = 0.0
      for (cf in chordFreqs) {
        pad += sin(2.0 * PI * cf * t) * 0.06
      }

      val mix = (kick + bass + snare + hihat + pad).coerceIn(-1.0, 1.0)
      val sampleShort = (mix * 30000.0).toInt().toShort()

      pcmData[i * 2] = sampleShort
      pcmData[i * 2 + 1] = sampleShort
    }

    writeWavFile(file, pcmData, sampleRate, 2)
  }

  private fun generateCyberpunkWav(file: File, baseBpm: Int, durationSec: Int) {
    val sampleRate = 44100
    val totalSamples = sampleRate * durationSec
    val beatSamples = (sampleRate * 60.0 / baseBpm).toInt()
    val pcmData = ShortArray(totalSamples * 2)

    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val beatPos = (i % beatSamples).toDouble() / beatSamples
      val beatIndex = (i / beatSamples) % 16

      // Driving techno / cyberpunk kick
      val kickT = beatPos * (60.0 / baseBpm)
      val kick = sin(2.0 * PI * (150.0 * exp(-kickT * 30.0) + 42.0) * kickT) * exp(-kickT * 9.0) * 0.85

      // Sawtooth-like driving bassline
      val bassStep = (i / (beatSamples / 4)) % 16
      val baseNote = if (beatIndex < 8) 55.0 else 49.0
      val noteFreq = baseNote * if (bassStep % 2 == 1) 1.25 else 1.0
      var saw = 0.0
      for (h in 1..6) {
        saw += (sin(2.0 * PI * noteFreq * h * t) / h) * 0.12
      }
      val bassT = (i % (beatSamples / 4)).toDouble() / sampleRate
      val bass = saw * exp(-bassT * 6.0)

      // Clang metallic perc on 2 & 4
      var clap = 0.0
      if (beatIndex % 4 == 2) {
        val cT = beatPos * (60.0 / baseBpm)
        clap = (Math.random() * 2.0 - 1.0) * exp(-cT * 18.0) * 0.45
      }

      // High cyber arpeggio
      val arpIndex = (i / (beatSamples / 4)) % 8
      val arpFreqs = doubleArrayOf(440.0, 523.25, 659.25, 783.99, 880.0, 783.99, 659.25, 523.25)
      val arp = sin(2.0 * PI * arpFreqs[arpIndex] * t) * 0.09

      val mix = (kick + bass + clap + arp).coerceIn(-1.0, 1.0)
      val sampleShort = (mix * 29000.0).toInt().toShort()
      pcmData[i * 2] = sampleShort
      pcmData[i * 2 + 1] = sampleShort
    }

    writeWavFile(file, pcmData, sampleRate, 2)
  }

  private fun generateLoFiWav(file: File, baseBpm: Int, durationSec: Int) {
    val sampleRate = 44100
    val totalSamples = sampleRate * durationSec
    val beatSamples = (sampleRate * 60.0 / baseBpm).toInt()
    val pcmData = ShortArray(totalSamples * 2)

    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val beatPos = (i % beatSamples).toDouble() / beatSamples
      val beatIndex = (i / beatSamples) % 8

      // Soft warm kick
      var kick = 0.0
      if (beatIndex == 0 || beatIndex == 3 || beatIndex == 6) {
        val kickT = beatPos * (60.0 / baseBpm)
        kick = sin(2.0 * PI * (75.0 * exp(-kickT * 14.0) + 40.0) * kickT) * exp(-kickT * 7.0) * 0.65
      }

      // Smooth electric Rhodes / jazz chords
      val chordT = t
      val chord1 = (sin(2.0 * PI * 196.0 * chordT) + sin(2.0 * PI * 246.94 * chordT) + sin(2.0 * PI * 293.66 * chordT) + sin(2.0 * PI * 369.99 * chordT)) * 0.08
      val chord2 = (sin(2.0 * PI * 174.61 * chordT) + sin(2.0 * PI * 220.0 * chordT) + sin(2.0 * PI * 261.63 * chordT) + sin(2.0 * PI * 329.63 * chordT)) * 0.08
      val currentChord = if (beatIndex < 4) chord1 else chord2

      // Warm acoustic upright bass
      val bassFreq = if (beatIndex < 4) 49.0 else 43.65
      val bass = (sin(2.0 * PI * bassFreq * t) * 0.55 + sin(2.0 * PI * bassFreq * 2.0 * t) * 0.2) * 0.6

      // Rimshot snare
      var rim = 0.0
      if (beatIndex == 2 || beatIndex == 6) {
        val rimT = beatPos * (60.0 / baseBpm)
        rim = (Math.random() * 2.0 - 1.0) * exp(-rimT * 25.0) * 0.35
      }

      // Subtle vinyl crackle
      val crackle = if (Math.random() < 0.003) (Math.random() * 2.0 - 1.0) * 0.05 else 0.0

      val mix = (kick + currentChord + bass + rim + crackle).coerceIn(-1.0, 1.0)
      val sampleShort = (mix * 28000.0).toInt().toShort()
      pcmData[i * 2] = sampleShort
      pcmData[i * 2 + 1] = sampleShort
    }

    writeWavFile(file, pcmData, sampleRate, 2)
  }

  private fun generateTrap808Wav(file: File, baseBpm: Int, durationSec: Int) {
    val sampleRate = 44100
    val totalSamples = sampleRate * durationSec
    val beatSamples = (sampleRate * 60.0 / baseBpm).toInt()
    val pcmData = ShortArray(totalSamples * 2)

    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val beatIndex = (i / beatSamples) % 16
      val beatPos = (i % beatSamples).toDouble() / beatSamples

      // Sustained 808 Sub-Bass Note with pitch glide
      var sub808 = 0.0
      val subToneIndex = (beatIndex / 4)
      val subBaseFreq = when (subToneIndex) {
        0 -> 36.71 // D1
        1 -> 32.70 // C1
        2 -> 41.20 // E1
        else -> 36.71
      }
      val subT = (i % (beatSamples * 4)).toDouble() / sampleRate
      // Sub distortion / saturation
      val rawSine = sin(2.0 * PI * subBaseFreq * t)
      val saturated = Math.tanh(rawSine * 2.2) * 0.8
      sub808 = saturated * exp(-subT * 0.5)

      // Punchy top kick
      var topKick = 0.0
      if (beatIndex % 4 == 0) {
        val kT = beatPos * (60.0 / baseBpm)
        topKick = sin(2.0 * PI * (160.0 * exp(-kT * 35.0) + 60.0) * kT) * exp(-kT * 12.0) * 0.7
      }

      // Trap snare on 4 and 12
      var snare = 0.0
      if (beatIndex == 4 || beatIndex == 12) {
        val sT = beatPos * (60.0 / baseBpm)
        snare = (Math.random() * 2.0 - 1.0) * exp(-sT * 15.0) * 0.55
      }

      // Fast rolling hi-hats
      val hatT = (i % (beatSamples / 4)).toDouble() / sampleRate
      val hat = (Math.random() * 2.0 - 1.0) * exp(-hatT * 80.0) * 0.22

      val mix = (sub808 + topKick + snare + hat).coerceIn(-1.0, 1.0)
      val sampleShort = (mix * 31000.0).toInt().toShort()
      pcmData[i * 2] = sampleShort
      pcmData[i * 2 + 1] = sampleShort
    }

    writeWavFile(file, pcmData, sampleRate, 2)
  }

  // RIFF WAV standard 44-byte header generator
  private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int, channels: Int) {
    val totalAudioLen = pcmData.size * 2
    val totalDataLen = totalAudioLen + 36
    val byteRate = sampleRate * channels * 2

    val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
    header.put("RIFF".toByteArray())
    header.putInt(totalDataLen)
    header.put("WAVE".toByteArray())
    header.put("fmt ".toByteArray())
    header.putInt(16) // Subchunk1Size for PCM
    header.putShort(1.toShort()) // AudioFormat 1 = PCM
    header.putShort(channels.toShort())
    header.putInt(sampleRate)
    header.putInt(byteRate)
    header.putShort((channels * 2).toShort()) // BlockAlign
    header.putShort(16.toShort()) // BitsPerSample
    header.put("data".toByteArray())
    header.putInt(totalAudioLen)

    val dataBytes = ByteBuffer.allocate(totalAudioLen).order(ByteOrder.LITTLE_ENDIAN)
    for (sample in pcmData) {
      dataBytes.putShort(sample)
    }

    FileOutputStream(file).use { fos ->
      fos.write(header.array())
      fos.write(dataBytes.array())
      fos.flush()
    }
  }
}

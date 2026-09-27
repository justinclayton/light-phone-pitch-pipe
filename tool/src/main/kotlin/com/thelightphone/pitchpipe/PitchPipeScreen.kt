package com.thelightphone.pitchpipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.audio.DefaultLightAudio
import com.thelightphone.sdk.audio.LightAudio
import com.thelightphone.sdk.audio.LightAudioItem
import com.thelightphone.sdk.audio.LightAudioSource
import com.thelightphone.sdk.audio.LightAudioUsage
import com.thelightphone.sdk.audio.LightMediaMetadata
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PipeNote(
    val label: String,
    val asset: String,
)

/**
 * Pitch Pipe: one chromatic octave, C4-B4, A4 = 440 Hz.
 *
 * Tap a note to sound it; it drones until tapped again (or another note is
 * tapped). The player has no repeat mode, so each tone file contains an exact
 * integer number of cycles and is queued back-to-back many times - PCM queue
 * transitions are gapless, which makes a continuous drone.
 */
class PitchPipeViewModel(
    audio: LightAudio,
) : LightViewModel<Unit>() {

    private val player = audio.newPlayer(usage = LightAudioUsage.Music)

    val notes = listOf(
        PipeNote("C", "tones/c4.wav"),
        PipeNote("C♯", "tones/cs4.wav"),
        PipeNote("D", "tones/d4.wav"),
        PipeNote("D♯", "tones/ds4.wav"),
        PipeNote("E", "tones/e4.wav"),
        PipeNote("F", "tones/f4.wav"),
        PipeNote("F♯", "tones/fs4.wav"),
        PipeNote("G", "tones/g4.wav"),
        PipeNote("G♯", "tones/gs4.wav"),
        PipeNote("A", "tones/a4.wav"),
        PipeNote("A♯", "tones/as4.wav"),
        PipeNote("B", "tones/b4.wav"),
    )

    private val _activeIndex = MutableStateFlow<Int?>(null)
    val activeIndex: StateFlow<Int?> = _activeIndex.asStateFlow()

    fun onNoteTapped(index: Int) {
        if (_activeIndex.value == index) {
            player.stop()
            _activeIndex.value = null
            return
        }
        val note = notes[index]
        val item = LightAudioItem(
            source = LightAudioSource.AssetSource(note.asset),
            metadata = LightMediaMetadata(title = "${note.label}4", artist = "Pitch Pipe"),
        )
        player.setMediaQueue(items = List(QUEUE_REPEATS) { item }, startIndex = 0)
        player.play()
        _activeIndex.value = index
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }

    private companion object {
        // 3s per file: enough queue for a ten-minute drone before it runs out.
        const val QUEUE_REPEATS = 200
    }
}

@InitialScreen
class PitchPipeScreen(private val sealedActivity: SealedLightActivity) :
    LightScreen<Unit, PitchPipeViewModel>(sealedActivity) {

    override val viewModelClass: Class<PitchPipeViewModel>
        get() = PitchPipeViewModel::class.java

    override fun createViewModel() = PitchPipeViewModel(DefaultLightAudio(sealedActivity))

    @Composable
    override fun Content() {
        val active by viewModel.activeIndex.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()
        val notes = viewModel.notes

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                LightText(
                    text = "Pitch Pipe",
                    variant = LightTextVariant.Heading,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    notes.chunked(COLUMNS).forEachIndexed { rowIndex, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            row.forEachIndexed { colIndex, note ->
                                val index = rowIndex * COLUMNS + colIndex
                                NoteCell(
                                    note = note,
                                    isActive = index == active,
                                    onTap = { viewModel.onNoteTapped(index) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                )
                            }
                        }
                    }
                }

                val footer = active?.let { "${notes[it].label}4 sounding - tap it to stop" }
                    ?: "Tap a note to sound it"
                LightText(
                    text = footer,
                    variant = LightTextVariant.Detail,
                    lighten = true,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                )
            }
        }
    }

    @Composable
    private fun NoteCell(
        note: PipeNote,
        isActive: Boolean,
        onTap: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val colors = LightThemeTokens.colors
        Box(
            modifier = modifier
                .padding(4.dp)
                .background(if (isActive) colors.content else colors.background)
                .lightClickable(onClick = onTap),
            contentAlignment = Alignment.Center,
        ) {
            LightText(
                text = note.label,
                variant = LightTextVariant.Subtitle,
                color = if (isActive) colors.background else colors.content,
            )
        }
    }

    private companion object {
        const val COLUMNS = 3
    }
}

package jez.synthesis.features.sequencer

import android.media.AudioTrack
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jez.synthesis.Consumer
import jez.synthesis.audiotrack.AudioInput
import jez.synthesis.audiotrack.AudioTrackGenerator
import jez.synthesis.audiotrack.Note
import jez.synthesis.audiotrack.Sampler
import jez.synthesis.features.sequencer.SequencerVM.Event
import jez.synthesis.persistence.Repository
import jez.synthesis.toViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SequencerVM(repository: Repository) : Consumer<Event>, ViewModel() {
    private val stateFlow = MutableStateFlow(
        State(
            sampler = null,
            samplers = emptyList(),
            beatCount = 16,
            toneCount = 24,
            bassNote = Note.A,
            octave = 3,
            input = emptyList(),
            isPlaying = false,
            isProcessing = false,
            bpm = 130,
        )
    )
    val viewState: StateFlow<SequencerViewState> =
        stateFlow.toViewState(viewModelScope) { SequencerStateToViewState(it) }

    var tracks = listOf<AudioTrack>()

    init {
        viewModelScope.launch {
            repository.getSamplers().collect {
                accept(Event.SamplerListUpdate(it))
            }
        }
    }

    override fun accept(value: Event) {
        viewModelScope.launch {
            stateFlow.value = processEvent(stateFlow.value, value)
            updateAudioTrack(stateFlow.value)
        }
    }

    private fun updateAudioTrack(state: State) {
        tracks.forEach { it.stop() }
        if (state.sampler != null && state.isPlaying) {
            // rebuild sequence
            val trackNotes = (0 until state.toneCount).map { y ->
                val trackInput = state.input.filter { it.y == y }
                if (trackInput.isEmpty()) {
                    emptyList()
                } else {
                    val frequency = state.bassNote.frequency(state.octave, y)
                    (0 until state.beatCount).map { x ->
                        val outputFreq = if (trackInput.any { it.x == x }) {
                            frequency
                        } else {
                            0.0
                        }
                        AudioInput(1, outputFreq)
                    }
                }
            }
            tracks = trackNotes
                .filter { it.isNotEmpty() }
                .map {
                    AudioTrackGenerator.generateTrack(
                        sessionId = 1,
                        bpm = state.bpm,
                        sampler = state.sampler,
                        input = it,
                    ).also { audioTrack ->
                        audioTrack.play()
                    }
                }
        }
    }

    private suspend fun processEvent(state: State, event: Event): State =
        when (event) {
            is Event.SamplerListUpdate -> state.copy(
                samplers = event.samplers,
                sampler = state.sampler ?: event.samplers.firstOrNull(),
            )
            is Event.SetGridCell -> state.copy(
                input = state.input.toMutableList().apply {
                    if (event.enabled) {
                        add(event.position)
                    } else {
                        remove(event.position)
                    }
                }
            )
            is Event.SetIsPlaying -> state.copy(isPlaying = event.isPlaying)
        }

    data class State(
        val sampler: Sampler?,
        val samplers: List<Sampler>,
        val beatCount: Int,
        val toneCount: Int,
        val bassNote: Note,
        val octave: Int,
        val input: List<IntOffset>,
        val isPlaying: Boolean,
        val isProcessing: Boolean,
        val bpm: Int,
    )

    sealed class Event {
        data class SamplerListUpdate(val samplers: List<Sampler>) : Event()
        data class SetGridCell(val position: IntOffset, val enabled: Boolean) : Event()
        data class SetIsPlaying(val isPlaying: Boolean) : Event()
    }
}

@Stable
data class SequencerViewState(
    val grid: Grid,
    val isPlaying: Boolean,
) {
    data class Grid(
        val width: Int,
        val height: Int,
        val selected: List<IntOffset>,
    )
}

object SequencerStateToViewState : (SequencerVM.State) -> SequencerViewState {
    override fun invoke(state: SequencerVM.State): SequencerViewState =
        SequencerViewState(
            isPlaying = state.isPlaying,
            grid = SequencerViewState.Grid(
                width = state.beatCount,
                height = state.toneCount,
                selected = state.input,
            )
        )
}

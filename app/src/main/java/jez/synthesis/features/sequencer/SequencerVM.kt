package jez.synthesis.features.sequencer

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jez.synthesis.Consumer
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
            input = emptyList()
        )
    )
    val viewState: StateFlow<SequencerViewState> =
        stateFlow.toViewState(viewModelScope) { SequencerStateToViewState(it) }

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
        }
    }

    private suspend fun processEvent(state: State, event: Event): State =
        when (event) {
            is Event.SamplerListUpdate -> state.copy(samplers = event.samplers)
            is Event.SetGridCell -> state.copy(
                input = state.input.toMutableList().apply {
                    if (event.enabled) {
                        add(event.position)
                    } else {
                        remove(event.position)
                    }
                }
            )
        }

    data class State(
        val sampler: Sampler?,
        val samplers: List<Sampler>,
        val beatCount: Int,
        val toneCount: Int,
        val bassNote: Note,
        val octave: Int,
        val input: List<IntOffset>,
    )

    sealed class Event {
        data class SamplerListUpdate(val samplers: List<Sampler>) : Event()
        data class SetGridCell(val position: IntOffset, val enabled: Boolean) : Event()
    }
}

@Stable
data class SequencerViewState(
    val grid: Grid,
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
            grid = SequencerViewState.Grid(
                width = state.beatCount,
                height = state.toneCount,
                selected = state.input,
            )
        )
}

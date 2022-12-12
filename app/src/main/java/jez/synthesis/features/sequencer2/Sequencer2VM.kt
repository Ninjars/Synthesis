package jez.synthesis.features.sequencer2

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumble.appyx.navmodel.backstack.BackStack
import jez.synthesis.Consumer
import jez.synthesis.audiotrack.Sampler
import jez.synthesis.features.sequencer2.Sequencer2VM.Event
import jez.synthesis.navigation.NavTarget
import jez.synthesis.toViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Sequencer2VM(
    private val backStack: BackStack<NavTarget>,
) : Consumer<Event>, ViewModel() {

    private val stateFlow = MutableStateFlow(
        State(
            isPlaying = false,
            beatCount = 10,
            toneCount = 10,
            input = emptyList(),
        )
    )
    val viewState: StateFlow<Sequencer2ViewState> =
        stateFlow.toViewState(viewModelScope) { StateToViewState(it) }

    val engineHandle = startEngine(100)

    override fun accept(value: Event) {
        TODO("Not yet implemented")
    }

    data class State(
//        val sampler: Sampler?,
//        val samplers: List<Sampler>,
        val beatCount: Int,
        val toneCount: Int,
//        val bassNote: Note,
//        val octave: Int,
        val input: List<IntOffset>,
        val isPlaying: Boolean,
//        val isProcessing: Boolean,
//        val bpm: Int,
    )

    sealed class Event {
        data class SamplerListUpdate(val samplers: List<Sampler>) : Event()
        data class SetGridCell(val position: IntOffset, val enabled: Boolean) : Event()
        data class SetIsPlaying(val isPlaying: Boolean) : Event()
        data class SelectedSampler(val index: Int) : Event()
        object EditSampler : Event()
        object CreateNewSampler : Event()
        object Reset : Event()
    }

    private companion object {
        external fun startEngine(cellCount: Int): Long
    }
}

@Stable
data class Sequencer2ViewState(
    val grid: Grid,
    val isPlaying: Boolean,
//    val sequencerIndex: Int,
//    val sequencerNames: List<String>,
) {
    data class Grid(
        val width: Int,
        val height: Int,
        val selected: List<IntOffset>,
    )
}

private object StateToViewState : (Sequencer2VM.State) -> Sequencer2ViewState {
    override fun invoke(state: Sequencer2VM.State): Sequencer2ViewState =
        Sequencer2ViewState(
            isPlaying = state.isPlaying,
            grid = Sequencer2ViewState.Grid(
                width = state.beatCount,
                height = state.toneCount,
                selected = state.input,
            ),
        )
}

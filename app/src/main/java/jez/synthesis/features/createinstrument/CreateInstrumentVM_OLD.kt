package jez.synthesis.features.createinstrument
//
//import androidx.compose.runtime.Stable
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import jez.synthesis.Consumer
//import jez.synthesis.audio.AudioEngine
//import jez.synthesis.audio.SynthEventData
//import jez.synthesis.audio.SynthInstrumentData
//import jez.synthesis.audio.SynthInstrumentData.OscillatorProps.WaveForm
//import jez.synthesis.audio.TimeSignature
//import jez.synthesis.features.createinstrument.CreateInstrumentVM.Event
//import jez.synthesis.features.createinstrument.CreateInstrumentViewState.InstrumentAttribute
//import jez.synthesis.toViewState
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import nl.igorski.mwengine.core.Pitch
//import java.util.*
//
//class CreateInstrumentVM(private val audioEngine: AudioEngine) : Consumer<Event>, ViewModel() {
//    private val stateFlow = MutableStateFlow(
//        State(
//            data = SynthInstrumentData(
//                id = UUID.randomUUID().toString(),
//                name = "Instrument",
//                oscillators = listOf(SynthInstrumentData.OscillatorProps(WaveForm.Sawtooth)),
//                processors = emptyList(),
//            )
//        )
//    )
//    val viewState: StateFlow<CreateInstrumentViewState> =
//        stateFlow.toViewState(viewModelScope) { CreateInstrumentStateToViewState(it) }
//
//    init {
////        audioEngine.reset()
//        audioEngine.setTimeSignature(
//            TimeSignature(
//                bpm = 120f,
//                beatCount = 2,
//                beatUnit = 2,
//                stepsPerBar = 16,
//                barsPerLoop = 1,
//            )
//        )
//    }
//
//    override fun accept(value: Event) {
////        viewModelScope.launch {
//        stateFlow.value = processEvent(stateFlow.value, value)
//        updateIsPlaying(stateFlow.value)
////        }
//    }
//
//    private fun processEvent(state: State, event: Event): State =
//        when (event) {
//            is Event.InstrumentLoaded -> State(data = event.data).also { createSynthEvent(it.data.id) }
//            is Event.ToggleIsPlaying -> state.copy(isPlaying = !state.isPlaying)
//            is Event.UpdateName -> state.copy(data = state.data.copy(name = event.value))
//            is Event.UpdateAttack -> state.copy(
//                data = state.data.copy(
//                    attackTime = event.value,
//                    attackEnabled = event.enabled
//                )
//            )
//            is Event.UpdateSustain -> state.copy(
//                data = state.data.copy(
//                    sustainLevel = event.value,
//                    sustainEnabled = event.enabled
//                )
//            )
//            is Event.UpdateRelease -> state.copy(
//                data = state.data.copy(
//                    releaseTime = event.value,
//                    releaseEnabled = event.enabled
//                )
//            )
//            is Event.UpdateDecay -> state.copy(
//                data = state.data.copy(
//                    decayTime = event.value,
//                    decayEnabled = event.enabled
//                )
//            )
//        }
//
//    private fun createSynthEvent(synthId: String) {
//        audioEngine.setSynthLoopEvents(
//            synthId,
//            listOf(
//                SynthEventData(
//                    frequency = Pitch.note("C", 4).toFloat(),
//                    position = 1,
//                    duration = 2f,
//                )
//            )
//        )
//    }
//
//    private fun updateIsPlaying(state: State) {
//        val isNew = !audioEngine.isSynthRegistered(state.data.id)
//        audioEngine.createOrUpdateInstrument(state.data)
//
//        if (isNew) {
//            createSynthEvent(state.data.id)
//        }
//
//        audioEngine.setIsPlaying(state.isPlaying)
//    }
//
//    data class State(
//        val isPlaying: Boolean = false,
//        val data: SynthInstrumentData,
//    )
//
//    sealed class Event {
//        data class InstrumentLoaded(val data: SynthInstrumentData) : Event()
//        object ToggleIsPlaying : Event()
//        data class UpdateName(val value: String) : Event()
//        data class UpdateAttack(val value: Float, val enabled: Boolean) : Event()
//        data class UpdateSustain(val value: Float, val enabled: Boolean) : Event()
//        data class UpdateRelease(val value: Float, val enabled: Boolean) : Event()
//        data class UpdateDecay(val value: Float, val enabled: Boolean) : Event()
//    }
//}
//
//
//@Stable
//data class CreateInstrumentViewState(
//    val isPlaying: Boolean,
//    val name: String,
//    val attack: InstrumentAttribute,
//    val sustain: InstrumentAttribute,
//    val release: InstrumentAttribute,
//    val decay: InstrumentAttribute,
//) {
//    data class InstrumentAttribute(
//        val value: String = "/",
//        val fraction: Float = 0f,
//        val enabled: Boolean = false,
//        val minValue: Float = 0f,
//    )
//}
//
//object CreateInstrumentStateToViewState : (CreateInstrumentVM.State) -> CreateInstrumentViewState {
//    override fun invoke(state: CreateInstrumentVM.State): CreateInstrumentViewState =
//        with(state.data) {
//            CreateInstrumentViewState(
//                isPlaying = state.isPlaying,
//                name = name,
//                attack = attackTime.toAttribute(AudioEngine.MinAttackTime, attackEnabled),
//                sustain = sustainLevel.toAttribute(AudioEngine.MinSustainLevel, sustainEnabled),
//                release = releaseTime.toAttribute(AudioEngine.MinReleaseTime, releaseEnabled),
//                decay = decayTime.toAttribute(AudioEngine.MinDecayTime, decayEnabled),
//            )
//        }
//
//    private fun Float?.toAttribute(minValue: Float, enabled: Boolean) =
//        if (this == null) {
//            InstrumentAttribute()
//        } else {
//            InstrumentAttribute(
//                value = this.toDisplayString(enabled),
//                fraction = this.toDisplayFraction(),
//                enabled = enabled,
//                minValue = minValue,
//            )
//        }
//
//    private fun Float?.toDisplayString(enabled: Boolean) =
//        if (this == null || !enabled) "/" else "%.2f".format(this)
//
//    private fun Float?.toDisplayFraction() = this?.coerceIn(0f, 1f) ?: 0f
//}

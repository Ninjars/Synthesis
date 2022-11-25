package jez.synthesis.features.createinstrument

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jez.synthesis.Consumer
import jez.synthesis.audio.SynthInstrumentData
import jez.synthesis.audiotrack.AudioGenerator
import jez.synthesis.audiotrack.Instrument
import jez.synthesis.audiotrack.Note
import jez.synthesis.audiotrack.Oscillator
import jez.synthesis.audiotrack.OscillatorParams
import jez.synthesis.audiotrack.OscillatorParams.Waveform
import jez.synthesis.audiotrack.OscillatorParams.WaveformParams
import jez.synthesis.audiotrack.Sampler
import jez.synthesis.features.createinstrument.CreateInstrumentVM.Event
import jez.synthesis.features.createinstrument.CreateInstrumentViewState.InstrumentAttribute
import jez.synthesis.toViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class CreateInstrumentVM : Consumer<Event>, ViewModel() {
    private val stateFlow = MutableStateFlow(
        State(
            sampleRate = 22000,
            data = SynthInstrumentData(
                id = UUID.randomUUID().toString(),
                name = "Instrument",
                oscillators = listOf(
                    OscillatorParams(
                        UUID.randomUUID().toString(),
                        listOf(
                            WaveformParams(Waveform.SINE, 1.0)
                        )
                    )
                ),
                processors = emptyList(),
            )
        )
    )
    val viewState: StateFlow<CreateInstrumentViewState> =
        stateFlow.toViewState(viewModelScope) { CreateInstrumentStateToViewState(it) }

    val audioGenerator = AudioGenerator(41000)
    val instrument = Instrument(audioGenerator, 1)

    init {
        updateInstrument(stateFlow.value)
    }

    override fun accept(value: Event) {
        viewModelScope.launch {
            stateFlow.value = processEvent(stateFlow.value, value)
            updateInstrument(stateFlow.value)
        }
    }

    private fun processEvent(state: State, event: Event): State =
        when (event) {
            is Event.Play -> state.also { instrument.play(1f, Note.A.frequency(4)) }
            is Event.UpdateName -> state.copy(data = state.data.copy(name = event.value))
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
            is Event.UpdateFade -> state.copy(
                data = state.data.copy(
                    fade = event.value,
                    fadeEnabled = event.enabled
                )
            )
            is Event.ReorderWaveforms -> state.copy(
                data = state.data.copy(
                    oscillators = state.data.oscillators.map {
                        if (it.id == event.oscillatorId) {
                            it.copy(
                                waveforms = it.waveforms.toMutableList().apply {
                                    add(event.to, removeAt(event.from))
                                }
                            )
                        } else {
                            it
                        }
                    }
                )
            )
            is Event.AddOscillator -> state.copy(
                data = state.data.copy(
                    oscillators = state.data.oscillators + listOf(OscillatorParams())
                )
            )
            is Event.DeleteOscillator -> state.copy(
                data = state.data.copy(
                    oscillators = state.data.oscillators.filterNot { it.id == event.oscillatorId }
                )
            )
            is Event.AddWaveform -> state.updateOscillator(event.oscillatorId) {
                it.copy(
                    waveforms = it.waveforms + listOf(WaveformParams())
                )
            }
            is Event.RemoveWaveform -> state.updateOscillator(event.oscillatorId) {
                it.copy(
                    waveforms = it.waveforms.dropLast(1)
                )
            }
            is Event.UpdateWaveform -> state.updateOscillator(event.oscillatorId) {
                it.copy(
                    waveforms = it.waveforms.map { waveform ->
                        if (waveform.id == event.waveformId) {
                            waveform.copy(
                                waveform = event.waveform,
                                multiplier = event.multiplier,
                                backoff = event.backoff,
                            )
                        } else {
                            waveform
                        }
                    }
                )
            }
        }

    private fun State.updateOscillator(id: String, block: (OscillatorParams) -> OscillatorParams) =
        copy(
            data = data.copy(
                oscillators = data.oscillators.map {
                    if (it.id == id) {
                        block(it)
                    } else {
                        it
                    }
                }
            )
        )

    private fun createSynthEvent(synthId: String) {
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
    }

    private fun updateInstrument(state: State) {
        val sampler = Sampler(
            state.sampleRate,
            state.data.oscillators.map {
                Oscillator(params = it)
            }
        )
        instrument.sampler = sampler
        instrument.fade = if (state.data.fadeEnabled) state.data.fade.toDouble() else 1.0
    }

    data class State(
        val isPlaying: Boolean = false,
        val sampleRate: Int,
        val data: SynthInstrumentData,
    )

    sealed class Event {
        object Play : Event()
        data class UpdateName(val value: String) : Event()

        //        data class UpdateAttack(val value: Float, val enabled: Boolean) : Event()
//        data class UpdateSustain(val value: Float, val enabled: Boolean) : Event()
//        data class UpdateRelease(val value: Float, val enabled: Boolean) : Event()
//        data class UpdateDecay(val value: Float, val enabled: Boolean) : Event()
        data class UpdateFade(val value: Float, val enabled: Boolean) : Event()
        data class ReorderWaveforms(val oscillatorId: String, val from: Int, val to: Int) : Event()
        data class DeleteOscillator(val oscillatorId: String) : Event()
        object AddOscillator : Event()
        data class AddWaveform(val oscillatorId: String) : Event()
        data class RemoveWaveform(val oscillatorId: String) : Event()
        data class UpdateWaveform(
            val oscillatorId: String,
            val waveformId: String,
            val waveform: Waveform,
            val multiplier: Double,
            val backoff: Double,
        ) : Event()
    }
}


@Stable
data class CreateInstrumentViewState(
    val isPlaying: Boolean,
    val name: String,
//    val attack: InstrumentAttribute,
//    val sustain: InstrumentAttribute,
//    val release: InstrumentAttribute,
//    val decay: InstrumentAttribute,
    val fade: InstrumentAttribute,
    val oscillators: List<OscillatorParams>,
    val visualisedWaveform: DoubleArray,
) {
    data class InstrumentAttribute(
        val textValue: String = "/",
        val value: Float = 0f,
        val enabled: Boolean = false,
        val minValue: Float = 0f,
        val maxValue: Float = 1f,
    )
}

object CreateInstrumentStateToViewState : (CreateInstrumentVM.State) -> CreateInstrumentViewState {
    override fun invoke(state: CreateInstrumentVM.State): CreateInstrumentViewState =
        with(state.data) {
            val sampler = Sampler(
                500,
                state.data.oscillators.map {
                    Oscillator(params = it)
                }
            )
            val samples = sampler.sample(1f, 10.0, state.data.fade.toDouble())


            CreateInstrumentViewState(
                isPlaying = state.isPlaying,
                name = name,
//                attack = attackTime.toAttribute(AudioEngine.MinAttackTime, attackEnabled),
//                sustain = sustainLevel.toAttribute(AudioEngine.MinSustainLevel, sustainEnabled),
//                release = releaseTime.toAttribute(AudioEngine.MinReleaseTime, releaseEnabled),
//                decay = decayTime.toAttribute(AudioEngine.MinDecayTime, decayEnabled),
                fade = fade.toAttribute(0f, 10f, fadeEnabled),
                oscillators = oscillators,
                visualisedWaveform = samples,
            )
        }

    private fun Float?.toAttribute(minValue: Float, maxValue: Float, enabled: Boolean) =
        if (this == null) {
            InstrumentAttribute()
        } else {
            InstrumentAttribute(
                textValue = this.toDisplayString(enabled),
                value = this,
                enabled = enabled,
                minValue = minValue,
                maxValue = maxValue,
            )
        }

    private fun Float?.toDisplayString(enabled: Boolean) =
        if (this == null || !enabled) "/" else "%.2f".format(this)
}

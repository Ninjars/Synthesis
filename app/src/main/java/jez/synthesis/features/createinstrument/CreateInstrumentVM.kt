package jez.synthesis.features.createinstrument

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumble.appyx.navmodel.backstack.BackStack
import jez.synthesis.Consumer
import jez.synthesis.audio.SynthInstrumentData
import jez.synthesis.audiotrack.AudioGenerator
import jez.synthesis.audiotrack.Note
import jez.synthesis.audiotrack.Oscillator
import jez.synthesis.audiotrack.Oscillator.Waveform
import jez.synthesis.audiotrack.Oscillator.WaveformParams
import jez.synthesis.audiotrack.Sampler
import jez.synthesis.features.createinstrument.CreateInstrumentVM.Event
import jez.synthesis.features.createinstrument.CreateInstrumentViewState.InstrumentAttribute
import jez.synthesis.navigation.NavTarget
import jez.synthesis.persistence.Repository
import jez.synthesis.toViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class CreateInstrumentVM(
    private val repository: Repository,
    private val backStack: BackStack<NavTarget>,
    existingId: String?,
) : Consumer<Event>, ViewModel() {
    private val stateFlow = MutableStateFlow(
        State(
            sampleRate = 22000,
            samplerId = UUID.randomUUID().toString(),
            data = SynthInstrumentData(
                id = UUID.randomUUID().toString(),
                name = "Instrument",
                oscillators = listOf(
                    Oscillator(
                        UUID.randomUUID().toString(),
                        listOf(
                            WaveformParams(Waveform.SINE, 1.0)
                        )
                    )
                ),
            )
        )
    )
    val viewState: StateFlow<CreateInstrumentViewState> =
        stateFlow.toViewState(viewModelScope) { CreateInstrumentStateToViewState(it) }

    private val audioGenerator = AudioGenerator(41000).apply { createPlayer(1) }

    init {
        viewModelScope.launch {
            val existingSampler = existingId?.let {
                repository.getSampler(it)
            }
            if (existingSampler != null) {
                accept(Event.LoadedSampler(existingSampler))
            } else {
                repository.storeSampler(stateFlow.value.toSampler())
            }
        }
    }

    override fun accept(value: Event) {
        viewModelScope.launch {
            stateFlow.value = processEvent(stateFlow.value, value)
            repository.storeSampler(stateFlow.value.toSampler())
        }
    }

    private fun processEvent(state: State, event: Event): State =
        when (event) {
            is Event.Play -> state.also {
                audioGenerator.writeSound(it.toSampler().sample(1f, Note.A.frequency(3)))
            }
            is Event.UpdateName -> state.copy(data = state.data.copy(name = event.value))
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
                    oscillators = state.data.oscillators + listOf(Oscillator())
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
            is Event.LoadedSampler -> with(event.sampler) {
                state.copy(
                    samplerId = id,
                    sampleRate = sampleRate,
                    data = state.data.copy(oscillators = oscillators)
                )
            }
        }

    private fun State.updateOscillator(id: String, block: (Oscillator) -> Oscillator) =
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

    private fun State.toSampler() =
        Sampler(
            id = samplerId,
            name = data.name,
            sampleRate = sampleRate,
            oscillators = data.oscillators
        )

    data class State(
        val isPlaying: Boolean = false,
        val sampleRate: Int,
        val samplerId: String,
        val data: SynthInstrumentData,
    )

    sealed class Event {
        object Play : Event()
        data class LoadedSampler(val sampler: Sampler) : Event()
        data class UpdateName(val value: String) : Event()

        //        data class UpdateAttack(val value: Float, val enabled: Boolean) : Event()
//        data class UpdateSustain(val value: Float, val enabled: Boolean) : Event()
//        data class UpdateRelease(val value: Float, val enabled: Boolean) : Event()
//        data class UpdateDecay(val value: Float, val enabled: Boolean) : Event()
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
//    val fade: InstrumentAttribute,
    val oscillators: List<Oscillator>,
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
                id = "demo",
                name = "",
                sampleRate = 500,
                oscillators = state.data.oscillators,
            )
            val samples = sampler.sample(1f, 10.0)


            CreateInstrumentViewState(
                isPlaying = state.isPlaying,
                name = name,
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

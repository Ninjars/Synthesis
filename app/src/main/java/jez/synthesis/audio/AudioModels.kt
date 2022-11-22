package jez.synthesis.audio

data class TimeSignature(
    val bpm: Float,
    val beatCount: Int,
    val beatUnit: Int,
    val stepsPerBar: Int,
    val barsPerLoop: Int,
) {
    val stepsPerBeat = stepsPerBar / beatCount
}

/**
 * Frequency is the pitch of the note. mwengine.core.Pitch provides helpers for this.
 * Duration is in subdivisions per measure
 * Position is subdivision index within measure
 *
 * "Measure" is effectively a musical bar, with subdivisions being the
 * shortest note supported therein.
 */
data class SynthEventData(
    val frequency: Float,
    val position: Int,
    val duration: Float,
)

data class SynthInstrumentData(
    val id: String,
    val oscillators: List<OscillatorProps>,
    val attackTime: Float? = null,
    val sustainLevel: Float? = null,
    val releaseTime: Float? = null,
    val decayTime: Float? = null,
) {
    data class OscillatorProps(
        val waveform: WaveForm,
    ) {
        enum class WaveForm {
            Sine,
            Triangle,
            Sawtooth,
            Square,
            Noise,
            PulseWidthModulation,
            KarplusStrong,
            Table,
        }
    }
}
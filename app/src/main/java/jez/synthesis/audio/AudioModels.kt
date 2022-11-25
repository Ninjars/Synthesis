package jez.synthesis.audio

import jez.synthesis.audiotrack.Oscillator

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
    val name: String,
    val oscillators: List<Oscillator>,
//    val processors: List<ProcessorData>,
//    val fade: Float = 1f,
//    val fadeEnabled: Boolean = false,
//    val attackTime: Float? = null,
//    val sustainLevel: Float? = null,
//    val releaseTime: Float? = null,
//    val decayTime: Float? = null,
//    val attackEnabled: Boolean = false,
//    val sustainEnabled: Boolean = false,
//    val releaseEnabled: Boolean = false,
//    val decayEnabled: Boolean = false,
)

sealed class ProcessorData {
    abstract val id: String

    data class PhaserData(
        override val id: String,
        val rate: Float,
        val feedback: Float,
        val depth: Float,
        val min: Float,
        val max: Float,
    ) : ProcessorData()
}

package jez.synthesis.audiotrack

import jez.synthesis.audiotrack.OscillatorParams.Waveform
import java.util.*
import kotlin.math.sin

/**
 * attack and fade are normalised progress amounts to fade in and out
 */
data class OscillatorParams(
    val id: String = UUID.randomUUID().toString(),
    var waveforms: List<WaveformParams> = listOf(WaveformParams()),
) {
    data class WaveformParams(
        val waveform: Waveform = Waveform.SINE,
        val multiplier: Double = 1.0,
        val id: String = UUID.randomUUID().toString(),
    )

    enum class Waveform {
        SINE,
    }
}

data class Oscillator(
    val sampleRate: Int,
    val params: OscillatorParams,
) {
    val id = params.id

    fun sample(sampleCount: Int, frequency: Double): List<Double> {
        val step = frequency / sampleRate
        return List(sampleCount) { i ->
            val cyclePosition = 2 * Math.PI * step * i
            getValue(cyclePosition)
        }
    }

    private fun getValue(cycle: Double) =
        params.waveforms
            .foldRight(0.0) { next, acc ->
                getValue(
                    waveform = next.waveform,
                    multiplier = next.multiplier,
                    inputValue = acc,
                    cycle = cycle,
                )
            }

    private fun getValue(
        waveform: Waveform,
        multiplier: Double,
        inputValue: Double,
        cycle: Double,
    ) =
        when (waveform) {
            Waveform.SINE ->
                sin(cycle * multiplier + inputValue)
        }
}

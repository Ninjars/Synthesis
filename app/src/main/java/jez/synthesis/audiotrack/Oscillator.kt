package jez.synthesis.audiotrack

import jez.synthesis.audiotrack.OscillatorParams.Waveform
import java.util.*
import kotlin.math.cos
import kotlin.math.exp
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
        val backoff: Double = 1.0,
        val id: String = UUID.randomUUID().toString(),
    )

    enum class Waveform {
        SINE,
        COS,
    }
}

data class Oscillator(
    val params: OscillatorParams,
) {
    val id = params.id

    fun sample(sampleRate: Int, sampleCount: Int, frequency: Double): List<Double> {
        val step = frequency / sampleRate
        return List(sampleCount) { i ->
            val normalisedTime = i / (sampleCount - 1).toDouble()
            val cyclePosition = 2 * Math.PI * step * i
            getValue(cyclePosition, normalisedTime)
        }
    }

    private fun getValue(cycle: Double, normalisedTime: Double) =
        params.waveforms
            .foldRight(0.0) { next, acc ->
                getValue(
                    waveform = next.waveform,
                    multiplier = next.multiplier,
                    backoff = next.backoff,
                    inputValue = acc,
                    cycle = cycle,
                    normalisedTime = normalisedTime,
                )
            }

    private fun getValue(
        waveform: Waveform,
        multiplier: Double,
        backoff: Double,
        inputValue: Double,
        cycle: Double,
        normalisedTime: Double,
    ) =
        when (waveform) {
            Waveform.SINE ->
                sin(cycle * multiplier + inputValue) * exp(-normalisedTime * backoff)
            Waveform.COS ->
                cos(cycle * multiplier + inputValue) * exp(-normalisedTime * backoff)
        }
}

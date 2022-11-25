package jez.synthesis.audiotrack

import kotlin.math.roundToInt

data class Sampler(
    val sampleRate: Int,
    val oscillators: List<Oscillator>,
) {
    private val attackPopSamples = sampleRate * AttackPopFraction
    private val decayPopSamples = sampleRate * DecayPopFraction

    fun sample(
        duration: Float,
        frequency: Double,
    ): DoubleArray {
        val sampleCount = (duration * sampleRate).roundToInt()
        return if (oscillators.isEmpty())
            DoubleArray(0)
        else
            oscillators.map { it.sample(sampleRate, sampleCount, frequency) }.reduce { a, b ->
                a.zip(b) { c, d -> c + d }
            }.mapIndexed { index, value ->
                val attackPopFactor =
                    lerpClamped(0.0, 1.0, (index / attackPopSamples).coerceIn(0.0, 1.0))
                val decayPopStart = sampleCount - 1 - decayPopSamples
                val decayPopFactor =
                    lerpClamped(1.0, 0.0, ((index - decayPopStart) / decayPopSamples))
                value / (oscillators.size).toDouble() * attackPopFactor * decayPopFactor
            }.toDoubleArray()
    }

    private fun lerpClamped(a: Double, b: Double, factor: Double): Double {
        return (a * (1.0 - factor).coerceIn(0.0, 1.0) + b * factor.coerceIn(0.0, 1.0))
    }

    private companion object {
        const val AttackPopFraction = 0.002
        const val DecayPopFraction = 0.01
    }
}

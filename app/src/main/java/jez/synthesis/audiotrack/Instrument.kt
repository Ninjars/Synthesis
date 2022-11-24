package jez.synthesis.audiotrack

import timber.log.Timber
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

class Instrument(
    private val audioGenerator: AudioGenerator,
    private val id: Int,
) {
    private val sampleRate = audioGenerator.sampleRate
    private var oscillators: List<Oscillator> = emptyList()
    var fade: Double = 1.0

    init {
        audioGenerator.createPlayer(id)
    }

    fun setOscillators(oscillators: List<OscillatorParams>) {
        this.oscillators = oscillators.map {
            Oscillator(
                sampleRate = sampleRate,
                params = it,
            )
        }
    }

    fun play(duration: Float, frequency: Double) {
        Timber.i("play $duration $frequency")
        if (oscillators.isEmpty()) return

        audioGenerator.writeSound(createSamples(duration, frequency))
    }

    private fun createSamples(duration: Float, frequency: Double): DoubleArray {
        val sampleCount = (duration * sampleRate).roundToInt()

        return oscillators.map { it.sample(sampleCount, frequency) }.reduce { a, b ->
            a.zip(b) { c, d -> c + d }
        }.mapIndexed { index, value ->
            val t = index / (sampleCount - 1).toDouble()
            value / (oscillators.size).toDouble() * exp(-t * fade)
        }.toDoubleArray()
    }

    private fun getSineWave(duration: Float, frequency: Double): DoubleArray {
        val sampleCount = (duration * sampleRate).roundToInt()
        val sample = DoubleArray(sampleCount)
        for (i in 0 until sampleCount) {
            sample[i] = sin(2 * Math.PI * i / (sampleRate / frequency))
        }
        return sample
    }

    private fun getBellSamples(duration: Float, frequency: Double): DoubleArray {
        val sampleCount = (duration * sampleRate).roundToInt()
        val sample = DoubleArray(sampleCount)
        val step = frequency / sampleRate
        for (i in 0 until sampleCount) {
            val cyclePosition = 2 * Math.PI * step * i
            val time = i / (sampleCount - 1).toDouble()
            sample[i] =
                sin(cyclePosition + sin(cyclePosition * 4) * exp(-time * 4)) * exp(-time * 7)
        }
        return sample
    }
}

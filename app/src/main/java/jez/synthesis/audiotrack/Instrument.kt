package jez.synthesis.audiotrack

import timber.log.Timber

class Instrument(
    private val audioGenerator: AudioGenerator,
    private val id: Int,
) {
    var sampler: Sampler? = null

    init {
        audioGenerator.createPlayer(id)
    }

    fun play(duration: Float, frequency: Double) {
        Timber.i("play $duration $frequency")
        sampler?.let {
            audioGenerator.writeSound(it.sample(duration, frequency))
        }
    }
}

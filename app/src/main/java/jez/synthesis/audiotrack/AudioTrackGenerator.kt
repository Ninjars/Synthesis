package jez.synthesis.audiotrack

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

data class AudioInput(
    val beatCount: Int,
    val frequency: Double,
)

object AudioTrackGenerator {
    fun generateTrack(
        sessionId: Int,
        bpm: Int,
        sampler: Sampler,
        input: List<AudioInput>,
    ): AudioTrack {
        val samplesPerBeat = ((bpm / 60.0) * sampler.sampleRate).toInt()
        val samples = input.flatMap {
            sampler.sample(it.beatCount * samplesPerBeat, it.frequency).asIterable()
        }.toDoubleArray()
        val bytes = AudioGenerator.get16BitPcm(samples)!!

        return AudioTrack(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(sampler.sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            bytes.size,
            AudioTrack.MODE_STATIC,
            sessionId
        ).also {
            it.write(
                bytes,
                0,
                bytes.size,
                AudioTrack.WRITE_BLOCKING,
            )
        }
    }
}
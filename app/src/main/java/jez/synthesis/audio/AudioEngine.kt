package jez.synthesis.audio

import android.app.Activity
import nl.igorski.mwengine.MWEngine
import nl.igorski.mwengine.core.BaseProcessor
import nl.igorski.mwengine.core.Drivers
import nl.igorski.mwengine.core.LPFHPFilter
import nl.igorski.mwengine.core.Limiter
import nl.igorski.mwengine.core.SampleManager
import nl.igorski.mwengine.core.SynthEvent
import nl.igorski.mwengine.core.SynthInstrument
import timber.log.Timber

class AudioEngine(
    private val bufferSize: Int,
    private val sampleRate: Int,
    private var timeSignature: TimeSignature,
) {
    private val engineStateObserver: MWEngineStateObserver =
        MWEngineStateObserver { dispose() }
    private val engine = MWEngine(engineStateObserver)
    private var isPlaying = false

    /**
     * Strong references must be kept to everything that connect to JNI objects
     * as the garbage collection of the JVM object will destroy the native one,
     * and native references to the native object are not tracked by the JVM.
     **/
    private val synths = HashMap<String, SynthInstrument>()
    private val synthEvents = HashMap<String, MutableList<SynthEvent>>()
    private val synthProcessors = HashMap<String, BaseProcessor>()
    private val masterFilter by lazy {
        LPFHPFilter(
            MWEngine.SAMPLE_RATE.toFloat(),
            55f,
            OutputChannels
        )
    }
    private val masterLimiter by lazy { Limiter() }

    init {
        engine.createOutput(
            sampleRate,
            bufferSize,
            OutputChannels,
            InputChannels,
            Drivers.types.AAUDIO
        )

        setTimeSignature(timeSignature)

        with(engine.masterBusProcessors) {
            // create a lowpass filter to catch all low rumbling and a limiter to prevent clipping of output :)
            addProcessor(masterFilter)
            addProcessor(masterLimiter)
        }
        engine.start()
    }

    fun togglePlaying() {
        isPlaying = !isPlaying
        Timber.i("start playing")
        engine.sequencerController.setPlaying(isPlaying)
    }

    fun setIsPlaying(play: Boolean) {
        Timber.i("setIsPlaying $play")
        engine.sequencerController.setPlaying(play)
    }

    fun setTimeSignature(timeSignature: TimeSignature) {
        this.timeSignature = timeSignature
        with(engine.sequencerController) {
            setTempoNow(timeSignature.bpm, timeSignature.beatCount, timeSignature.beatUnit)
            updateMeasures(timeSignature.barsPerLoop, timeSignature.stepsPerBar)
        }
    }

    fun isSynthRegistered(id: String) = synths.contains(id)

    fun createOrUpdateInstrument(instrument: SynthInstrumentData): SynthInstrument {
        Timber.i("createOrUpdateInstrument $instrument")
        val synth = synths.getOrDefault(instrument.id, SynthInstrument())
        instrument.oscillators.forEachIndexed { index, data ->
            synth.getOscillatorProperties(index).waveform = data.waveform.ordinal
        }
        with(synth.adsr) {
            attackTime = if (instrument.attackEnabled && instrument.attackTime != null) {
                instrument.attackTime.coerceAtLeast(MinAttackTime)
            } else {
                MinAttackTime
            }
            sustainLevel = if (instrument.sustainEnabled && instrument.sustainLevel != null) {
                instrument.sustainLevel.coerceAtLeast(MinSustainLevel)
            } else {
                MinSustainLevel
            }
            releaseTime = if (instrument.releaseEnabled && instrument.releaseTime != null) {
                instrument.releaseTime.coerceAtLeast(MinReleaseTime)
            } else {
                MinReleaseTime
            }
            decayTime = if (instrument.decayEnabled && instrument.decayTime != null) {
                instrument.decayTime.coerceAtLeast(MinDecayTime)
            } else {
                MinDecayTime
            }
        }
        val processors = instrument.processors.map {
            it.id to mapProcessorDataToInstance(it, synthProcessors[it.id])
        }

        synth.audioChannel.processingChain.reset() // might need to invoke delete() instead
        processors.forEach { (_, processor) ->
            synth.audioChannel.processingChain.addProcessor(processor)
        }
        synthProcessors.putAll(processors)
        synths[instrument.id] = synth
        return synth
    }

    fun addSynthLoopEvents(synthId: String, events: List<SynthEventData>) {
        Timber.i("addSynthLoopEvents for $synthId ${events.size}")
        val synth = synths[synthId]
        if (synth == null) {
            Timber.e("synth with id $synthId not registered")
            return
        }

        val existing = synthEvents.getOrPut(synthId) { mutableListOf() }
        existing.addAll(
            events.map {
                SynthEvent(
                    it.frequency,
                    it.position,
                    it.duration,
                    synth,
                )
            }
        )
        Timber.i("> events $existing")
    }

    fun setSynthLoopEvents(synthId: String, events: List<SynthEventData>) {
        Timber.i("setSynthLoopEvents for $synthId ${events.size}")
        val synth = synths[synthId]
        if (synth == null) {
            Timber.e("synth with id $synthId not registered")
            return
        }
        synthEvents[synthId]?.forEach { it.delete() }

        synthEvents[synthId] = events.map {
            SynthEvent(
                it.frequency,
                it.position,
                it.duration,
                synth,
            )
        }.toMutableList()
    }

    fun dispose() {
        Timber.i("dispose")
        reset()
        engine.dispose()
    }

    fun reset() {
        Timber.i("reset")
        engine.stop()

        synthEvents.values.flatten().forEach { it.delete() }
        synthEvents.clear()

        // detach all processors from engine's master bus
        engine.masterBusProcessors.reset()

        synths.values.forEach { it.delete() }
        synths.clear()

        synthProcessors.values.forEach { it.delete() }
        synthProcessors.clear()

        // flush sample memory allocated in the SampleManager
        SampleManager.flushSamples()
    }

    companion object {
        private const val OutputChannels = 2 // 1 for mono, 2 for stereo
        private const val InputChannels = 1


        const val MinAttackTime = 0.01f
        const val MinDecayTime = 0.0f
        const val MinSustainLevel = 0.1f
        const val MinReleaseTime = 0.01f

        fun create(activity: Activity, timeSignature: TimeSignature): AudioEngine =
            AudioEngine(
                bufferSize = MWEngine.getRecommendedBufferSize(activity.applicationContext),
                sampleRate = MWEngine.getRecommendedSampleRate(activity.applicationContext),
                timeSignature = timeSignature,
            ).also {
                MWEngine.optimizePerformance(activity)
            }
    }
}

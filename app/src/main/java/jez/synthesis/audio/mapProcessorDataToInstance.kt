package jez.synthesis.audio

import nl.igorski.mwengine.core.BaseProcessor
import nl.igorski.mwengine.core.Phaser

fun mapProcessorDataToInstance(data: ProcessorData, existing: BaseProcessor?): BaseProcessor =
    when (data) {
        is ProcessorData.PhaserData -> {
            (existing as? Phaser)?.let {
                it.apply {
                    rate = data.rate
                    feedback = data.feedback
                    depth = data.depth
                    setRange(data.min, data.max)
                }
            } ?: Phaser(
                data.rate,
                data.feedback,
                data.depth,
                data.min,
                data.max,
            )
        }
    }

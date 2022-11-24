package jez.synthesis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import jez.synthesis.features.createinstrument.CreateInstrumentScreen
import jez.synthesis.features.createinstrument.CreateInstrumentVM
import jez.synthesis.ui.theme.SynthesisTheme
import nl.igorski.mwengine.core.SynthEvent

class MainActivity : ComponentActivity() {
    private lateinit var liveEvent: SynthEvent
    private val audioEngine by lazy {
//        AudioEngine.create(
//            this,
//            TimeSignature(
//                bpm = 100f,
//                beatCount = 4,
//                beatUnit = 4,
//                stepsPerBar = 16,
//                barsPerLoop = 1,
//            )
//        )
//            .also {
//                        val instrumentData = SynthInstrumentData(
//                            id = "synth",
//                            name = "test instrument",
//                            oscillators = listOf(SynthInstrumentData.OscillatorProps(WaveForm.Triangle)),
//                            processors = emptyList(),
//                            attackTime = 0.2f,
//                            attackEnabled = true,
//                            releaseTime = 0.2f,
//                            releaseEnabled = true,
//                        )
//                        val synth = it.createOrUpdateInstrument(
//                            instrumentData
//                        )
//                        liveEvent = SynthEvent(Pitch.note("C", 3).toFloat(), synth)
//                        it.addSynthLoopEvents(
//                            instrumentData.id,
//                            listOf(
//                                SynthEventData(
//                                    frequency = Pitch.note("C", 4).toFloat(),
//                                    position = 0,
//                                    duration = 2f,
//                                )
//                            )
//                        )
//                  }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SynthesisTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
//                    Button(onClick = { audioEngine.togglePlaying() }) {
//                        Text(text = "Toggle Playing")
//                    }
                    CreateInstrumentScreen(CreateInstrumentVM())
                }
            }
        }
    }
}

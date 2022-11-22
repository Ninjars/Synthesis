package jez.synthesis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import jez.synthesis.audio.AudioEngine
import jez.synthesis.audio.SynthEventData
import jez.synthesis.audio.SynthInstrumentData
import jez.synthesis.audio.SynthInstrumentData.OscillatorProps.WaveForm
import jez.synthesis.audio.TimeSignature
import jez.synthesis.ui.theme.SynthesisTheme
import nl.igorski.mwengine.core.Pitch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SynthesisTheme {
                val audioEngine = remember {
                    AudioEngine.create(
                        this,
                        TimeSignature(
                            bpm = 130f,
                            beatCount = 4,
                            beatUnit = 4,
                            stepsPerBar = 16,
                            barsPerLoop = 1,
                        )
                    ).also {
                        val instrumentData = SynthInstrumentData(
                            id = "synth",
                            oscillators = listOf(SynthInstrumentData.OscillatorProps(WaveForm.PulseWidthModulation)),
                            releaseTime = 0.15f,
                        )
                        it.addInstrument(
                            instrumentData
                        )
                        it.addSynthLoopEvents(
                            instrumentData.id,
                            listOf(
                                SynthEventData(
                                    frequency = Pitch.note("C", 4).toFloat(),
                                    position = 0,
                                    duration = 1f,
                                )
                            )
                        )
                    }
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Button(onClick = { audioEngine.togglePlaying() }) {
                        Text(text = "Toggle Playing")
                    }
                }
            }
        }
    }
}

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
import jez.synthesis.persistence.InMemoryRepository
import jez.synthesis.ui.theme.SynthesisTheme

class MainActivity : ComponentActivity() {
    private val repository = InMemoryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SynthesisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CreateInstrumentScreen(CreateInstrumentVM(repository))
                }
            }
        }
    }
}

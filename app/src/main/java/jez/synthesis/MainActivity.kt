package jez.synthesis

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeComponentActivity
import jez.synthesis.navigation.RootNode
import jez.synthesis.persistence.InMemoryRepository
import jez.synthesis.ui.theme.SynthesisTheme

class MainActivity : NodeComponentActivity() {
    private val repository = InMemoryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SynthesisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    NodeHost(integrationPoint = appyxIntegrationPoint) {
                        RootNode(
                            repository = repository,
                            buildContext = it
                        )
                    }
////                    CreateInstrumentScreen(CreateInstrumentVM(repository))
//                    SequencerScreen(SequencerVM(repository))
                }
            }
        }
    }
}

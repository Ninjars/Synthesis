package jez.synthesis.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.BackStack
import jez.synthesis.features.createinstrument.CreateInstrumentScreen
import jez.synthesis.features.createinstrument.CreateInstrumentVM
import jez.synthesis.features.sequencer.SequencerScreen
import jez.synthesis.features.sequencer.SequencerVM
import jez.synthesis.persistence.Repository
import kotlinx.parcelize.Parcelize

sealed class NavTarget : Parcelable {
    @Parcelize
    object Sequencer : NavTarget()

    @Parcelize
    data class Instrument(val synthId: String?) : NavTarget()
}

class RootNode(
    private val repository: Repository,
    buildContext: BuildContext,
    private val backStack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.Sequencer,
        savedStateMap = buildContext.savedStateMap,
    )
) : ParentNode<NavTarget>(
    navModel = backStack,
    buildContext = buildContext,
) {
    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node =
        when (navTarget) {
            is NavTarget.Instrument -> node(buildContext) {
                CreateInstrumentScreen(
                    viewModel = CreateInstrumentVM(
                        repository,
                        backStack,
                        navTarget.synthId
                    )
                )
            }
            is NavTarget.Sequencer -> node(buildContext) {
                SequencerScreen(viewModel = SequencerVM(repository, backStack))
            }
        }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = navModel)
    }

}

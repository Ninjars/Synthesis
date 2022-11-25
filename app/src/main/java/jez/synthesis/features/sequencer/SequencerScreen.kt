package jez.synthesis.features.sequencer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import jez.synthesis.rememberEventConsumer

@Composable
fun SequencerScreen(viewModel: SequencerVM) {
    SequencerContent(
        viewModel.viewState.collectAsState(),
        rememberEventConsumer(viewModel)
    )
}

@Composable
fun SequencerContent(
    state: State<SequencerViewState>,
    eventHandler: (SequencerVM.Event) -> Unit,
) {
    NoteGrid(
        eventHandler = eventHandler,
    ) { state.value.grid }
}

@Composable
fun NoteGrid(
    eventHandler: (SequencerVM.Event) -> Unit,
    gridProvider: () -> SequencerViewState.Grid,
) {
    val grid = gridProvider()
    val selected = grid.selected
    val width = grid.width
    Column(modifier = Modifier.fillMaxWidth()) {
        (0 until grid.height).forEach { y ->
            Row(modifier = Modifier.fillMaxWidth()) {
                (0 until width).forEach { x ->
                    val offset = IntOffset(x, y)
                    val isSelected = selected.contains(offset)
                    NoteCell(
                        onClick = { eventHandler(SequencerVM.Event.SetGridCell(offset, it)) },
                        isSelected = isSelected
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.NoteCell(
    onClick: (Boolean) -> Unit,
    isSelected: Boolean,
) {
    Box(modifier = Modifier
        .weight(1f)
        .padding(1.dp)
        .aspectRatio(1f)
        .clickable { onClick(!isSelected) }
        .background(
            color = if (isSelected)
                MaterialTheme.colors.primary
            else
                MaterialTheme.colors.secondary
        )
    )
}

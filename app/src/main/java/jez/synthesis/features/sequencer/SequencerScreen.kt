package jez.synthesis.features.sequencer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import jez.synthesis.features.sequencer.SequencerVM.Event
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
    eventHandler: (Event) -> Unit,
) {
    Box {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SequencerSelector(
                    modifier = Modifier.weight(1f),
                    selectedIndexProvider = { state.value.sequencerIndex },
                    optionsProvider = { state.value.sequencerNames },
                    eventHandler = { eventHandler(Event.SelectedSampler(it)) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { eventHandler(Event.EditSampler) }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Sampler"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { eventHandler(Event.CreateNewSampler) }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create new Sampler"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { eventHandler(Event.Reset) }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            NoteGrid(
                eventHandler = eventHandler,
            ) { state.value.grid }
        }
        PlayPauseButton(eventHandler = eventHandler) { state.value.isPlaying }
    }
}

@Composable
private fun SequencerSelector(
    modifier: Modifier,
    selectedIndexProvider: () -> Int,
    optionsProvider: () -> List<String>,
    eventHandler: (Int) -> Unit,
) {
    val selectedIndex = selectedIndexProvider()
    val options = optionsProvider()

    var isExpanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(selectedIndex) }

    Button(
        modifier = modifier
            .padding(end = 8.dp),
        onClick = { isExpanded = !isExpanded },
    ) {
        Text(
            text = options.getOrElse(selectedIndex) { "---" },
        )
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            options.forEachIndexed { index, value ->
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        if (index != selected) {
                            eventHandler(index)
                            selected = index
                        }
                    },
                    contentPadding = PaddingValues(8.dp),
                    modifier = if (selected == index) {
                        Modifier.border(width = 2.dp, color = MaterialTheme.colors.onSurface)
                    } else {
                        Modifier
                    },
                ) {
                    Text(
                        text = options[index]
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.PlayPauseButton(
    eventHandler: (Event) -> Unit,
    isPlaying: () -> Boolean,
) {
    val playing = isPlaying()
    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
        onClick = { eventHandler(Event.SetIsPlaying(!playing)) }
    ) {
        Icon(
            imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "Play/Pause",
        )
    }
}

@Composable
fun NoteGrid(
    eventHandler: (Event) -> Unit,
    gridProvider: () -> SequencerViewState.Grid,
) {
    val grid = gridProvider()
    val selected = grid.selected
    val width = grid.width
    Column(modifier = Modifier.fillMaxWidth()) {
        (0 until grid.height).forEach { y ->
            Row(modifier = Modifier.fillMaxWidth()) {
                (0 until width).forEach { x ->
                    val offset = IntOffset(x, grid.height - y - 1)
                    val isSelected = selected.contains(offset)
                    NoteCell(
                        onClick = { eventHandler(Event.SetGridCell(offset, it)) },
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

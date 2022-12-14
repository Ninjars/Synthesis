package jez.synthesis.features.createinstrument

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import jez.synthesis.audiotrack.Oscillator
import jez.synthesis.features.createinstrument.CreateInstrumentVM.Event
import jez.synthesis.rememberEventConsumer

@Composable
fun CreateInstrumentScreen(viewModel: CreateInstrumentVM) {
    CreateInstrumentContent(
        viewModel.viewState.collectAsState(),
        rememberEventConsumer(viewModel),
    )
}

@Composable
fun CreateInstrumentContent(
    state: State<CreateInstrumentViewState>,
    eventHandler: (Event) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Name(eventHandler) { state.value.name }
            Visualizer { state.value.visualisedWaveform }
            Oscillators(eventHandler) { state.value.oscillators }

            Spacer(modifier = Modifier.height(64.dp))
        }

        PlayPauseButton(eventHandler) { state.value.isPlaying }
    }
}

@Composable
private fun BoxScope.PlayPauseButton(
    eventHandler: (Event) -> Unit,
    isPlaying: () -> Boolean,
) {
    FloatingActionButton(
        modifier = Modifier.align(Alignment.BottomEnd),
        onClick = { eventHandler(Event.Play) }
    ) {
        Icon(
            imageVector = if (isPlaying()) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "Play/Pause",
        )
    }
}

@Composable
private fun Name(
    eventHandler: (Event) -> Unit,
    currentName: () -> String,
) {
    TextField(
        value = currentName(),
        onValueChange = { eventHandler(Event.UpdateName(it)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
    )
}

@Composable
private fun Oscillators(
    eventHandler: (Event) -> Unit,
    oscillators: () -> List<Oscillator>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Oscillators")
            IconButton(onClick = { eventHandler(Event.AddOscillator) }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Append new oscillator"
                )
            }
        }
        oscillators().forEachIndexed { index, it ->
            Card {
                OscillatorController(eventHandler, it, index)
            }
        }
    }
}

@Composable
private fun OscillatorController(
    eventHandler: (Event) -> Unit,
    oscillator: Oscillator,
    index: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "$index.")
            IconButton(onClick = { eventHandler(Event.DeleteOscillator(oscillator.id)) }) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = "Delete Oscillator $index"
                )
            }
        }
        Waveforms(eventHandler = eventHandler, oscillator = oscillator)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { eventHandler(Event.AddWaveform(oscillator.id)) }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Append new waveform"
                )
            }
            IconButton(onClick = { eventHandler(Event.RemoveWaveform(oscillator.id)) }) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = "Delete last waveform"
                )
            }
        }
    }
}

@Composable
private fun Waveforms(
    eventHandler: (Event) -> Unit,
    oscillator: Oscillator,
) {
    Column {
        oscillator.waveforms.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                WaveformSelector(item.waveform) {
                    eventHandler(
                        Event.UpdateWaveform(
                            oscillator.id,
                            item.id,
                            it,
                            item.multiplier,
                            item.backoff,
                        )
                    )
                }
                Column {
                    Row {
                        Text(text = "Mult")
                        Slider(
                            value = item.multiplier.toFloat(),
                            valueRange = 0.01f..2f,
                            onValueChange = {
                                eventHandler(
                                    Event.UpdateWaveform(
                                        oscillator.id,
                                        item.id,
                                        item.waveform,
                                        it.toDouble(),
                                        item.backoff,
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        )
                        Text(text = "%.2f".format(item.multiplier))
                    }
                    Row {
                        Text(text = "Fade")
                        Slider(
                            value = item.backoff.toFloat(),
                            valueRange = 0f..10f,
                            onValueChange = {
                                eventHandler(
                                    Event.UpdateWaveform(
                                        oscillator.id,
                                        item.id,
                                        item.waveform,
                                        item.multiplier,
                                        it.toDouble(),
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        )
                        Text(text = "%.2f".format(item.backoff))
                    }
                }
            }
        }
    }
}

@Composable
private fun WaveformSelector(
    selectedValue: Oscillator.Waveform,
    eventHandler: (Oscillator.Waveform) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(selectedValue) }

    Button(
        modifier = Modifier
            .padding(end = 8.dp),
        onClick = { isExpanded = !isExpanded },
    ) {
        Text(
            text = selectedValue.name,
        )
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            Oscillator.Waveform.values().forEach { value ->
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        if (value != selected) {
                            eventHandler(value)
                            selected = value
                        }
                    },
                    contentPadding = PaddingValues(8.dp),
                    modifier = if (selected == value) {
                        Modifier.border(width = 2.dp, color = MaterialTheme.colors.onSurface)
                    } else {
                        Modifier
                    },
                ) {
                    Text(
                        text = value.name
                    )
                }
            }
        }
    }
}

@Composable
fun Visualizer(
    pathPointsProvider: () -> DoubleArray
) {
    val lineColor = MaterialTheme.colors.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f)
            .background(MaterialTheme.colors.surface)
    ) {
        val pathPoints = pathPointsProvider()
        val max = pathPoints.max()
        val xFactor = size.width / pathPoints.size
        val yFactor = size.height / max.toFloat() / 2f
        val path = Path().apply {
            moveTo(0f, size.height - (pathPoints.first().toFloat() * yFactor + size.height / 2f))
            pathPoints.forEachIndexed { index, y ->
                lineTo(
                    index.toFloat() * xFactor,
                    size.height - (y.toFloat() * yFactor + size.height / 2f)
                )
            }
        }
        drawLine(
            brush = SolidColor(Color.LightGray),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
        )
        drawPath(
            path,
            brush = SolidColor(lineColor),
            style = Stroke(5f)
        )
    }
}

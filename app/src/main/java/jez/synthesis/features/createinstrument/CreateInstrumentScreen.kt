package jez.synthesis.features.createinstrument

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import jez.synthesis.features.createinstrument.CreateInstrumentVM.Event
import jez.synthesis.features.createinstrument.CreateInstrumentViewState.InstrumentAttribute
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
            AttributeController(
                "Attack",
                updateHandler = { value, enabled ->
                    eventHandler(
                        Event.UpdateAttack(
                            value = value,
                            enabled = enabled
                        )
                    )
                },
                attributeProvider = { state.value.attack }
            )
            AttributeController(
                "Sustain",
                updateHandler = { value, enabled ->
                    eventHandler(
                        Event.UpdateSustain(
                            value = value,
                            enabled = enabled
                        )
                    )
                },
                attributeProvider = { state.value.sustain }
            )
            AttributeController(
                "Release",
                updateHandler = { value, enabled ->
                    eventHandler(
                        Event.UpdateRelease(
                            value = value,
                            enabled = enabled
                        )
                    )
                },
                attributeProvider = { state.value.release }
            )
            AttributeController(
                "Decay",
                updateHandler = { value, enabled ->
                    eventHandler(
                        Event.UpdateDecay(
                            value = value,
                            enabled = enabled
                        )
                    )
                },
                attributeProvider = { state.value.decay },
            )

            Spacer(modifier = Modifier.height(64.dp))
        }

        PlayPauseButton(eventHandler) { state.value.isPlaying }
    }
}

@Composable
fun BoxScope.PlayPauseButton(
    eventHandler: (Event) -> Unit,
    isPlaying: () -> Boolean,
) {
    FloatingActionButton(
        modifier = Modifier.align(Alignment.BottomEnd),
        onClick = { eventHandler(Event.ToggleIsPlaying) }
    ) {
        Icon(
            imageVector = if (isPlaying()) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "Play/Pause",
        )
    }
}

@Composable
fun AttributeController(
    label: String,
    updateHandler: (Float, Boolean) -> Unit,
    attributeProvider: () -> InstrumentAttribute
) {
    val state = attributeProvider()
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row {
                Text(text = label)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = state.value)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Enabled: ")
                Checkbox(
                    checked = state.enabled,
                    onCheckedChange = { updateHandler(state.fraction, it) }
                )
            }
        }
        Slider(
            value = state.fraction,
            valueRange = state.minValue..1f,
            onValueChange = { updateHandler(it, true) },
            steps = 256,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = if (state.enabled) {
                SliderDefaults.colors()
            } else {
                val normalColors =
                    SliderDefaults.colors()
                SliderDefaults.colors(
                    activeTrackColor = normalColors.trackColor(
                        enabled = false,
                        active = true
                    ).value,
                    thumbColor = normalColors.thumbColor(enabled = false).value,
                )
            }
        )
    }
}

@Composable
fun Name(
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

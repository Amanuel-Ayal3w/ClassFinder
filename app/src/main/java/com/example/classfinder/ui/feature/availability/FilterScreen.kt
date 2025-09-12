package com.example.classfinder.ui.feature.availability

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // Added this import
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.classfinder.data.model.TimeMode
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: AvailabilityViewModel,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ui = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kifil Finder", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { Icon(Icons.Filled.Apartment, contentDescription = null) }
            )
        }
    ) { inner ->
    Column(
        modifier = modifier.fillMaxSize().padding(inner).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Find an available room", style = MaterialTheme.typography.titleMedium)

        if (ui.isLoading && ui.buildings.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            return@Column
        }

        // Building selector as radio list
        Text("Building")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ui.buildings.forEach { b ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onBuildingSelected(b.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = ui.selectedBuildingId == b.id,
                        onClick = { viewModel.onBuildingSelected(b.id) }
                    )
                    Text(text = b.name, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        // Time mode
        Text("When")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.onTimeModeChanged(TimeMode.NOW) }
            ) {
                RadioButton(selected = ui.timeMode == TimeMode.NOW, onClick = { viewModel.onTimeModeChanged(TimeMode.NOW) })
                Text("Now", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.onTimeModeChanged(TimeMode.NEXT_HOUR) }
            ) {
                RadioButton(selected = ui.timeMode == TimeMode.NEXT_HOUR, onClick = { viewModel.onTimeModeChanged(TimeMode.NEXT_HOUR) })
                Text("Next hour", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.onTimeModeChanged(TimeMode.CUSTOM) }
            ) {
                RadioButton(selected = ui.timeMode == TimeMode.CUSTOM, onClick = { viewModel.onTimeModeChanged(TimeMode.CUSTOM) })
                Text("Custom time", modifier = Modifier.padding(start = 8.dp))
            }

            if (ui.timeMode == TimeMode.CUSTOM) {
                var showPicker by remember { mutableStateOf(false) }
                val init = ui.customTime ?: LocalTime.now()
                val timePickerState: TimePickerState = rememberTimePickerState(
                    initialHour = init.hour,
                    initialMinute = init.minute,
                    is24Hour = false
                )
                val formatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
                val label = ui.customTime?.format(formatter) ?: "Pick time"

                TextButton(onClick = { showPicker = true }) {
                    Text(label)
                }

                if (showPicker) {
                    AlertDialog(
                        onDismissRequest = { showPicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.onCustomTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                                showPicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPicker = false }) { Text("Cancel") }
                        },
                        title = { Text("Select time") },
                        text = { TimePicker(state = timePickerState) }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSearch,
            enabled = ui.selectedBuildingId != null && (ui.timeMode != TimeMode.CUSTOM || ui.customTime != null)
        ) {
            Text("Find Rooms")
        }

        if (ui.error != null) {
            Text("Error: ${ui.error}")
        }
    }
    }
}




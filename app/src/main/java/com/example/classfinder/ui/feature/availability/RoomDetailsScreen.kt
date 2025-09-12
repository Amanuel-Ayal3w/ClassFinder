package com.example.classfinder.ui.feature.availability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

@Composable
fun RoomDetailsScreen(
    roomId: String,
    viewModel: AvailabilityViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ui = viewModel.uiState.collectAsState().value
    val room = ui.results.firstOrNull { it.room.id == roomId }?.room
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack) { Text("Back") }
        if (room == null) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Room not found")
            }
            return@Column
        }
        Text(text = room.name)
        Divider()
        Text("Building: ${room.buildingId}")
        // Future: show full schedule, amenities, etc.
    }
}

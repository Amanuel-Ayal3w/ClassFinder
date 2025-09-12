package com.example.classfinder.ui.feature.availability

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: AvailabilityViewModel,
    onBack: () -> Unit,
    onOpenDetails: (roomId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState() // Changed to delegate
    val formatter = remember { DateTimeFormatter.ofPattern("h:mm a") } // remember formatter
    var bookingForRoomId by remember { mutableStateOf<String?>(null) }
    var bookingDurationMinutes by remember { mutableStateOf(60) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Results") }, navigationIcon = {}) }
    ) { inner ->
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(inner)
            .padding(16.dp)
    ) {
        Button(onClick = onBack) { Text("Back") }

        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { Text("Loading...") }
            return@Column
        }

        if (uiState.results.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { Text("No rooms found for the chosen filters.") }
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.results) { item ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { onOpenDetails(item.room.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        val now = LocalTime.now()
                        val canBook = item.availableUntil.isAfter(now.plusMinutes(30))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onOpenDetails(item.room.id) }) { Text("Details") }
                            Button(onClick = { bookingForRoomId = item.room.id }, enabled = canBook) { Text("Book") }
                        }
                    }
                }
                Divider()
            }
        }

        // Booking dialog
        val currentRoomIdToBook = bookingForRoomId // Use a local val to ensure stability inside the if
        if (currentRoomIdToBook != null) {
            AlertDialog(
                onDismissRequest = { bookingForRoomId = null },
                confirmButton = {
                    TextButton(onClick = {
                        val start = LocalTime.now()
                        val end = start.plusMinutes(bookingDurationMinutes.toLong())
                        viewModel.bookRoom(currentRoomIdToBook, start, end)
                        bookingForRoomId = null
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { bookingForRoomId = null }) { Text("Cancel") }
                },
                title = { Text("Book room") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Choose duration")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30, 60, 90).forEach { minutes ->
                                val selected = bookingDurationMinutes == minutes
                                if (selected) {
                                    Button(onClick = { /* keep selected */ }) { Text("$minutes min") }
                                } else {
                                    OutlinedButton(onClick = { bookingDurationMinutes = minutes }) { Text("$minutes min") }
                                }
                            }
                        }
                        Text("Starts now at ${LocalTime.now().format(formatter)}")
                    }
                }
            )
        }
    }
    }
}




package com.example.classfinder.ui.feature.availability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.classfinder.data.MockRoomRepository
import com.example.classfinder.data.RoomRepository
import com.example.classfinder.data.model.Building
import com.example.classfinder.data.model.FilterCriteria
import com.example.classfinder.data.model.Booking
import com.example.classfinder.data.model.RoomAvailability
import com.example.classfinder.data.model.TimeMode
import com.example.classfinder.domain.AvailabilityCalculator
import java.time.LocalTime
import java.time.LocalDate
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AvailabilityUiState(
    val buildings: List<Building> = emptyList(),
    val selectedBuildingId: String? = null,
    val timeMode: TimeMode = TimeMode.NOW,
    val customTime: LocalTime? = null,
    val results: List<RoomAvailability> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AvailabilityViewModel(
    private val repo: RoomRepository = MockRoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvailabilityUiState())
    val uiState: StateFlow<AvailabilityUiState> = _uiState.asStateFlow()

    // Local in-memory bookings for MVP
    private val bookings = MutableStateFlow<List<Booking>>(emptyList())

    init {
        loadInitial()
    }

    private fun loadInitial() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching {
            val buildings = repo.getBuildings()
            _uiState.value.copy(
                buildings = buildings,
                selectedBuildingId = buildings.firstOrNull()?.id
            )
        }.onSuccess { updated ->
            _uiState.value = updated.copy(isLoading = false)
        }.onFailure { e ->
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    fun onBuildingSelected(id: String) {
        _uiState.value = _uiState.value.copy(selectedBuildingId = id)
    }

    fun onTimeModeChanged(mode: TimeMode) {
        _uiState.value = _uiState.value.copy(timeMode = mode)
    }

    fun onCustomTimeSelected(time: LocalTime) {
        _uiState.value = _uiState.value.copy(customTime = time)
    }

    fun computeResults() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching {
            val rooms = repo.getRooms()
            val schedule = repo.getSchedule() + bookingsToScheduleSlots()
            val s = _uiState.value
            val results = AvailabilityCalculator.availableRooms(
                rooms = rooms,
                schedule = schedule,
                buildingId = s.selectedBuildingId,
                mode = s.timeMode,
                customTime = s.customTime
            )
            _uiState.value.copy(results = results)
        }.onSuccess { updated ->
            _uiState.value = updated.copy(isLoading = false)
        }.onFailure { e ->
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    private fun bookingsToScheduleSlots(): List<com.example.classfinder.data.model.ScheduleSlot> {
        val today = LocalDate.now().dayOfWeek
        return bookings.value.filter { it.dayOfWeek == today }.map {
            com.example.classfinder.data.model.ScheduleSlot(
                roomId = it.roomId,
                dayOfWeek = it.dayOfWeek,
                start = it.start,
                end = it.end
            )
        }
    }

    fun bookRoom(roomId: String, start: LocalTime, end: LocalTime) = viewModelScope.launch {
        val today: DayOfWeek = LocalDate.now().dayOfWeek
        // Simple validation: start < end
        if (end.isAfter(start)) {
            bookings.value = bookings.value + Booking(roomId, today, start, end)
            // Recompute results to reflect new booking
            computeResults()
        }
    }

    fun cancelBookings(roomId: String) = viewModelScope.launch {
        bookings.value = bookings.value.filterNot { it.roomId == roomId }
        computeResults()
    }

    fun currentCriteria(): FilterCriteria {
        val s = _uiState.value
        return FilterCriteria(s.selectedBuildingId, s.timeMode, s.customTime)
    }
}

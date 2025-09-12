package com.example.classfinder.data.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Building(
    val id: String,
    val name: String
)

data class Room(
    val id: String,
    val buildingId: String,
    val name: String,
    val capacity: Int? = null,
    val floor: Int? = null
)

data class ScheduleSlot(
    val roomId: String,
    val dayOfWeek: DayOfWeek,
    val start: LocalTime,
    val end: LocalTime
)

enum class TimeMode { NOW, NEXT_HOUR, CUSTOM }

data class FilterCriteria(
    val buildingId: String?,
    val timeMode: TimeMode,
    val customTime: LocalTime?
)

data class RoomAvailability(
    val room: Room,
    val availableUntil: LocalTime
)

// Local-only booking to simulate reservations during MVP
data class Booking(
    val roomId: String,
    val dayOfWeek: DayOfWeek,
    val start: LocalTime,
    val end: LocalTime
)

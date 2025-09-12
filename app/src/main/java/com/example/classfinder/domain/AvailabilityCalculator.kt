package com.example.classfinder.domain

import com.example.classfinder.data.model.Room
import com.example.classfinder.data.model.RoomAvailability
import com.example.classfinder.data.model.ScheduleSlot
import com.example.classfinder.data.model.TimeMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

object AvailabilityCalculator {

    fun availableRooms(
        rooms: List<Room>,
        schedule: List<ScheduleSlot>,
        buildingId: String?,
        mode: TimeMode,
        customTime: LocalTime?,
        now: LocalTime = LocalTime.now(),
        day: DayOfWeek = LocalDate.now().dayOfWeek
    ): List<RoomAvailability> {
        val queryTime = when (mode) {
            TimeMode.NOW, TimeMode.NEXT_HOUR -> now
            TimeMode.CUSTOM -> customTime ?: now
        }
        val requireOneHour = mode == TimeMode.NEXT_HOUR
        val daySlots = schedule.filter { it.dayOfWeek == day }
        val endOfDay = LocalTime.of(23, 59)

        val filteredRooms = rooms.filter { buildingId == null || it.buildingId == buildingId }

        return filteredRooms.mapNotNull { room ->
            val slots = daySlots.filter { it.roomId == room.id }.sortedBy { it.start }

            if (slots.any { queryTime >= it.start && queryTime < it.end }) {
                return@mapNotNull null
            }

            val nextSlot = slots.firstOrNull { it.start > queryTime }
            val availableUntil = nextSlot?.start ?: endOfDay

            if (requireOneHour && availableUntil.isBefore(queryTime.plusHours(1))) {
                return@mapNotNull null
            }

            RoomAvailability(room, availableUntil)
        }.sortedBy { it.availableUntil }
    }
}

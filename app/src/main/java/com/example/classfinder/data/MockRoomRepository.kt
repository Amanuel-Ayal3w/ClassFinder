package com.example.classfinder.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.classfinder.data.model.Building
import com.example.classfinder.data.model.Room
import com.example.classfinder.data.model.ScheduleSlot
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.delay

object MockRoomRepository : RoomRepository {

    private val buildings = listOf(
        Building(id = "samsung", name = "Samsung Building"),
        Building(id = "nb", name = "NB Building"),
        Building(id = "main", name = "Main Building")
    )

    private val rooms = listOf(
        // Samsung Building rooms
        Room(id = "samsung-101", buildingId = "samsung", name = "Samsung 101", floor = 1),
        Room(id = "samsung-102", buildingId = "samsung", name = "Samsung 102", floor = 1),
        Room(id = "samsung-201", buildingId = "samsung", name = "Samsung 201", floor = 2),
        // NB Building rooms
        Room(id = "nb-201", buildingId = "nb", name = "NB 201", floor = 2),
        Room(id = "nb-202", buildingId = "nb", name = "NB 202", floor = 2),
        // Main Building rooms
        Room(id = "main-301", buildingId = "main", name = "Main 301", floor = 3),
        Room(id = "main-302", buildingId = "main", name = "Main 302", floor = 3)
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private val schedule = buildList {
        val weekdays = listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        weekdays.forEach { day ->
            // Samsung Building busy times
            add(ScheduleSlot("samsung-101", day, LocalTime.of(10, 0), LocalTime.of(12, 0)))
            add(ScheduleSlot("samsung-102", day, LocalTime.of(9, 0), LocalTime.of(10, 30)))
            // NB Building busy times
            add(ScheduleSlot("nb-201", day, LocalTime.of(14, 0), LocalTime.of(15, 0)))
            add(ScheduleSlot("nb-202", day, LocalTime.of(16, 0), LocalTime.of(18, 0)))
            // Main Building busy times
            add(ScheduleSlot("main-301", day, LocalTime.of(13, 0), LocalTime.of(17, 0)))
            // main-302 no schedule (often free)
        }
    }

    override suspend fun getBuildings(): List<Building> {
        delay(100)
        return buildings
    }

    override suspend fun getRooms(): List<Room> {
        delay(100)
        return rooms
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getSchedule(): List<ScheduleSlot> {
        delay(100)
        return schedule
    }
}

package com.example.classfinder.data

import com.example.classfinder.data.model.Building
import com.example.classfinder.data.model.Room
import com.example.classfinder.data.model.ScheduleSlot

interface RoomRepository {
    suspend fun getBuildings(): List<Building>
    suspend fun getRooms(): List<Room>
    suspend fun getSchedule(): List<ScheduleSlot>
}

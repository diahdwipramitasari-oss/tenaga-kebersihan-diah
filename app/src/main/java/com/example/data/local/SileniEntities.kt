package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cleaners")
data class CleanerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val employeeCode: String,
    val assignedZone: String, // "A", "B", "C"
    val zoneTitle: String,
    val shiftHours: String,
    val phoneExt: String,
    val initials: String,
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: String, // e.g., "R-01"
    val name: String,
    val floor: String,
    val zone: String, // "A", "B", "C"
    val category: String,
    val qrPayload: String,
    val isActive: Boolean = true,
)

@Entity(tableName = "feedbacks")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: String,
    val roomName: String,
    val zone: String,
    val rating: Int, // 3 = Puas, 2 = Cukup, 1 = Kurang
    val tagsCsv: String,
    val comment: String,
    val reporterRole: String, // "Pegawai Internal" or "Tamu Dinas"
    val timestampMillis: Long,
    val isResolved: Boolean = false,
)

@Entity(tableName = "checklist_tasks")
data class ChecklistTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val zone: String,
    val title: String,
    val timeSlot: String,
    val isCompleted: Boolean = false,
)

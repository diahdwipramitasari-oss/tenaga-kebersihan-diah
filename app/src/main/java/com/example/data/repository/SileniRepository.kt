package com.example.data.repository

import com.example.data.local.ChecklistTaskEntity
import com.example.data.local.CleanerEntity
import com.example.data.local.FeedbackEntity
import com.example.data.local.RoomEntity
import com.example.data.local.SileniDao
import com.example.data.local.SileniDatabase
import com.example.ui.state.DailyTrendPoint
import com.example.ui.state.DashboardSummary
import com.example.ui.state.OfficerPerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class SileniSnapshot(
    val cleaners: List<CleanerEntity>,
    val rooms: List<RoomEntity>,
    val feedbacks: List<FeedbackEntity>,
    val tasks: List<ChecklistTaskEntity>,
    val officerPerformances: List<OfficerPerformance>,
    val dashboardSummary: DashboardSummary,
)

class SileniRepository(private val dao: SileniDao) {

    suspend fun ensureSeeded() {
        SileniDatabase.ensureSeeded(dao)
    }

    fun observeSnapshot(): Flow<SileniSnapshot> = combine(
        dao.observeCleaners(),
        dao.observeRooms(),
        dao.observeFeedbacks(),
        dao.observeChecklistTasks(),
    ) { cleaners, rooms, feedbacks, tasks ->
        val performances = cleaners.map { cleaner ->
            val zoneRooms = rooms.filter { it.zone == cleaner.assignedZone }
            val zoneFeedbacks = feedbacks.filter { it.zone == cleaner.assignedZone }
            val zoneTasks = tasks.filter { it.zone == cleaner.assignedZone }
            val avg = if (zoneFeedbacks.isEmpty()) 3.0f
            else zoneFeedbacks.map { it.rating }.average().toFloat()

            OfficerPerformance(
                cleaner = cleaner,
                assignedRooms = zoneRooms,
                totalReviews = zoneFeedbacks.size,
                avgScore = avg,
                puasCount = zoneFeedbacks.count { it.rating == 3 },
                cukupCount = zoneFeedbacks.count { it.rating == 2 },
                kurangCount = zoneFeedbacks.count { it.rating == 1 },
                unresolvedAlerts = zoneFeedbacks.count { it.rating < 3 && !it.isResolved },
                completedTasks = zoneTasks.count { it.isCompleted },
                totalTasks = zoneTasks.size,
            )
        }

        val total = feedbacks.size
        val puas = feedbacks.count { it.rating == 3 }
        val cukup = feedbacks.count { it.rating == 2 }
        val kurang = feedbacks.count { it.rating == 1 }
        val avgOverall = if (total == 0) 0f else feedbacks.map { it.rating }.average().toFloat()
        val satPercent = if (total == 0) 0 else ((puas * 100f) / total).toInt()
        val unresolved = feedbacks.count { it.rating < 3 && !it.isResolved }

        val summary = DashboardSummary(
            averageScore = avgOverall,
            satisfactionPercent = satPercent,
            totalReviews = total,
            puasCount = puas,
            cukupCount = cukup,
            kurangCount = kurang,
            unresolvedAlerts = unresolved,
            dailyTrend = buildSevenDayTrend(feedbacks),
        )

        SileniSnapshot(
            cleaners = cleaners,
            rooms = rooms,
            feedbacks = feedbacks,
            tasks = tasks,
            officerPerformances = performances,
            dashboardSummary = summary,
        )
    }

    suspend fun submitFeedback(
        room: RoomEntity,
        rating: Int,
        tags: List<String>,
        comment: String,
        reporterRole: String,
    ) {
        val defaultComment = when {
            comment.isNotBlank() -> comment.trim()
            rating == 3 -> "Kondisi ruangan bersih, rapi, dan nyaman digunakan."
            rating == 2 -> "Kebersihan cukup baik, namun perlu perhatian pada detail area."
            else -> "Perlu pembersihan segera pada area ini."
        }
        dao.insertFeedback(
            FeedbackEntity(
                roomId = room.id,
                roomName = room.name,
                zone = room.zone,
                rating = rating.coerceIn(1, 3),
                tagsCsv = tags.joinToString(","),
                comment = defaultComment,
                reporterRole = reporterRole,
                timestampMillis = System.currentTimeMillis(),
                isResolved = rating == 3,
            )
        )
    }

    suspend fun markFeedbackResolved(feedbackId: Long, resolved: Boolean) {
        dao.updateFeedbackResolved(feedbackId, resolved)
    }

    suspend fun addRoom(
        roomId: String,
        name: String,
        floor: String,
        zone: String,
        category: String,
    ) {
        val cleanId = roomId.trim().uppercase(Locale.ROOT)
        val cleanZone = zone.trim().uppercase(Locale.ROOT)
        dao.insertRoom(
            RoomEntity(
                id = cleanId,
                name = name.trim(),
                floor = floor.trim(),
                zone = cleanZone,
                category = category.trim(),
                qrPayload = "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=$cleanId&zona=$cleanZone",
                isActive = true,
            )
        )
    }

    suspend fun updateRoomZone(roomId: String, newZone: String) {
        dao.updateRoomZone(roomId, newZone)
    }

    suspend fun toggleTaskCompleted(taskId: Long, completed: Boolean) {
        dao.updateTaskCompleted(taskId, completed)
    }

    private fun buildSevenDayTrend(feedbacks: List<FeedbackEntity>): List<DailyTrendPoint> {
        val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
        val cal = Calendar.getInstance()
        val points = mutableListOf<DailyTrendPoint>()

        for (daysAgo in 6 downTo 0) {
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            val end = start + 86_400_000L

            val dayFeedbacks = feedbacks.filter { it.timestampMillis in start until end }
            val avg = if (dayFeedbacks.isEmpty()) 2.6f else dayFeedbacks.map { it.rating }.average().toFloat()
            val label = if (daysAgo == 0) "Hari Ini" else dayFormat.format(cal.time).take(3)
            points.add(DailyTrendPoint(label = label, avgScore = avg, count = dayFeedbacks.size))
        }
        return points
    }
}

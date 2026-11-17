package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SileniDao {
    @Query("SELECT * FROM cleaners ORDER BY assignedZone ASC")
    fun observeCleaners(): Flow<List<CleanerEntity>>

    @Query("SELECT * FROM rooms ORDER BY zone ASC, id ASC")
    fun observeRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM feedbacks ORDER BY timestampMillis DESC")
    fun observeFeedbacks(): Flow<List<FeedbackEntity>>

    @Query("SELECT * FROM checklist_tasks ORDER BY zone ASC, id ASC")
    fun observeChecklistTasks(): Flow<List<ChecklistTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCleaners(cleaners: List<CleanerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedbacks(feedbacks: List<FeedbackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistTasks(tasks: List<ChecklistTaskEntity>)

    @Query("UPDATE feedbacks SET isResolved = :resolved WHERE id = :feedbackId")
    suspend fun updateFeedbackResolved(feedbackId: Long, resolved: Boolean)

    @Query("UPDATE rooms SET zone = :newZone WHERE id = :roomId")
    suspend fun updateRoomZone(roomId: String, newZone: String)

    @Query("UPDATE checklist_tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun updateTaskCompleted(taskId: Long, completed: Boolean)

    @Query("SELECT COUNT(*) FROM cleaners")
    suspend fun countCleaners(): Int
}

package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SileniDatabase
import com.example.data.repository.PlaceholderAiInsightRepository
import com.example.data.repository.SileniRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches SILENI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SILENI", appName)
    }

    @Test
    fun `seed database and submit feedback updates dashboard stats and AI insight`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, SileniDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = SileniRepository(db.sileniDao())
            repo.ensureSeeded()

            val initial = repo.observeSnapshot().first()
            assertEquals(3, initial.cleaners.size)
            assertEquals(9, initial.rooms.size)
            assertTrue(initial.feedbacks.isNotEmpty())

            val room = initial.rooms.first()
            repo.submitFeedback(
                room = room,
                rating = 3,
                tags = listOf("Lantai Bersih"),
                comment = "Sangat bersih dan nyaman.",
                reporterRole = "Pegawai Internal",
            )

            val updated = repo.observeSnapshot().first()
            assertEquals(initial.feedbacks.size + 1, updated.feedbacks.size)

            val aiReport = PlaceholderAiInsightRepository().generateExecutiveInsight(updated)
            assertTrue(aiReport.sampleLabel.contains("Sampel"))
            assertTrue(aiReport.recommendations.isNotEmpty())
        } finally {
            db.close()
        }
    }
}

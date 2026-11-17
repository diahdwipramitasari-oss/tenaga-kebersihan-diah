package com.example.ui.state

import com.example.data.local.ChecklistTaskEntity
import com.example.data.local.CleanerEntity
import com.example.data.local.FeedbackEntity
import com.example.data.local.RoomEntity

enum class UserPersona(
    val displayName: String,
    val subtitle: String,
    val badge: String,
) {
    KASUBBAG(
        displayName = "Kasubbag Umum",
        subtitle = "Monitoring Dashboard & Rapor Kinerja",
        badge = "Pimpinan / Penilai",
    ),
    ADMIN(
        displayName = "Admin Sistem",
        subtitle = "Master Data Ruangan & QR Generator",
        badge = "Administrator",
    ),
    PETUGAS(
        displayName = "Petugas Kebersihan",
        subtitle = "Jadwal, Checklist & Notifikasi Komplain",
        badge = "3 Personil",
    ),
    PUBLIK(
        displayName = "Pegawai / Tamu",
        subtitle = "Scan QR & Beri Penilaian Cepat",
        badge = "Publik Anonim",
    ),
}

data class DailyTrendPoint(
    val label: String,
    val avgScore: Float, // 1.0f..3.0f
    val count: Int,
)

data class OfficerPerformance(
    val cleaner: CleanerEntity,
    val assignedRooms: List<RoomEntity>,
    val totalReviews: Int,
    val avgScore: Float, // 1.0f..3.0f
    val puasCount: Int,
    val cukupCount: Int,
    val kurangCount: Int,
    val unresolvedAlerts: Int,
    val completedTasks: Int,
    val totalTasks: Int,
) {
    val satisfactionPercent: Int
        get() = if (totalReviews == 0) 100 else ((puasCount * 100f) / totalReviews).toInt()
}

data class DashboardSummary(
    val averageScore: Float,
    val satisfactionPercent: Int,
    val totalReviews: Int,
    val puasCount: Int,
    val cukupCount: Int,
    val kurangCount: Int,
    val unresolvedAlerts: Int,
    val dailyTrend: List<DailyTrendPoint>,
)

data class KeywordInsight(
    val keyword: String,
    val count: Int,
    val sentimentLabel: String, // "Positif", "Perlu Perhatian", "Keluhan"
)

data class AiInsightReport(
    val sampleLabel: String,
    val generatedAtLabel: String,
    val executiveSummary: String,
    val hotspotZoneTitle: String,
    val hotspotDetail: String,
    val keywordInsights: List<KeywordInsight>,
    val recommendations: List<String>,
    val monthlyOfficerNotes: List<String>,
    val shareableReportText: String,
)

sealed interface AiUiState {
    data object Loading : AiUiState
    data class Success(val report: AiInsightReport) : AiUiState
    data class Error(val message: String) : AiUiState
}

data class SileniUiState(
    val isLoading: Boolean = true,
    val activePersona: UserPersona = UserPersona.KASUBBAG,
    val cleaners: List<CleanerEntity> = emptyList(),
    val rooms: List<RoomEntity> = emptyList(),
    val feedbacks: List<FeedbackEntity> = emptyList(),
    val checklistTasks: List<ChecklistTaskEntity> = emptyList(),
    val officerPerformances: List<OfficerPerformance> = emptyList(),
    val dashboardSummary: DashboardSummary = DashboardSummary(
        averageScore = 0f,
        satisfactionPercent = 0,
        totalReviews = 0,
        puasCount = 0,
        cukupCount = 0,
        kurangCount = 0,
        unresolvedAlerts = 0,
        dailyTrend = emptyList(),
    ),
    val aiState: AiUiState = AiUiState.Loading,
    val selectedRoomIdForFeedback: String = "R-01",
    val selectedFeedbackFilter: Int? = null, // null = All, 3 = Puas, 2 = Cukup, 1 = Kurang
    val snackbarMessage: String? = null,
)

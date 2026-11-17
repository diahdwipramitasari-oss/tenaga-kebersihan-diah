package com.example.data.repository

import com.example.ui.state.AiInsightReport
import com.example.ui.state.KeywordInsight
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Interface abstraksi untuk layanan AI Insight SILENI.
 * Saat Firebase AI Logic diaktifkan, implementasi interface ini dapat langsung
 * dihubungkan ke Gemini (Firebase.ai.generativeModel).
 */
interface AiInsightRepository {
    suspend fun generateExecutiveInsight(snapshot: SileniSnapshot): AiInsightReport
}

/**
 * Implementasi sampel lokal yang digunakan ketika Firebase belum diaktifkan.
 * Mengolah data aktual dari Room DB menjadi ringkasan terstruktur dan rekomendasi
 * dengan label sampel yang jelas.
 */
class PlaceholderAiInsightRepository : AiInsightRepository {

    override suspend fun generateExecutiveInsight(snapshot: SileniSnapshot): AiInsightReport {
        delay(350) // Simulasi pemrosesan ringkas agar transisi UI terasa responsif

        val summary = snapshot.dashboardSummary
        val officers = snapshot.officerPerformances
        val feedbacks = snapshot.feedbacks

        val worstOfficer = officers.minByOrNull { it.avgScore }
        val bestOfficer = officers.maxByOrNull { it.avgScore }

        val complaintFeedbacks = feedbacks.filter { it.rating < 3 }
        val topComplaintRoom = complaintFeedbacks
            .groupBy { it.roomName }
            .maxByOrNull { it.value.size }

        val timeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))
        val nowLabel = timeFormat.format(Date())

        val keywordMap = linkedMapOf(
            "Bau / Aroma Toilet" to (listOf("bau", "aroma", "pesing") to "Keluhan"),
            "Lantai Licin / Basah" to (listOf("basah", "licin", "wudhu", "pel") to "Perlu Perhatian"),
            "Sampah / Pantry" to (listOf("sampah", "penuh", "gelas", "pantry") to "Keluhan"),
            "Sabun / Tisu Habis" to (listOf("sabun", "tisu", "habis", "wastafel") to "Keluhan"),
            "Debu / Kaca Partisi" to (listOf("debu", "berdebu", "kaca", "bercak") to "Perlu Perhatian"),
            "Bersih & Harum" to (listOf("bersih", "harum", "wangi", "rapi", "mengkilap", "nyaman") to "Positif"),
        )

        val keywordInsights = keywordMap.mapNotNull { (title, pair) ->
            val (terms, sentiment) = pair
            val matchCount = feedbacks.count { fb ->
                val text = "${fb.comment} ${fb.tagsCsv}".lowercase(Locale.ROOT)
                terms.any { text.contains(it) }
            }
            if (matchCount > 0) KeywordInsight(title, matchCount, sentiment) else null
        }.sortedByDescending { it.count }

        val executiveSummary = buildString {
            append("Dari total ${summary.totalReviews} penilaian pengguna ruangan di Disperindag Kab. Sidoarjo, ")
            append("indeks kepuasan mencapai ${summary.satisfactionPercent}% (skor rata-rata ${String.format(Locale.US, "%.2f", summary.averageScore)}/3.00). ")
            if (bestOfficer != null) {
                append("Performa tertinggi dicatat oleh ${bestOfficer.cleaner.name} (Zona ${bestOfficer.cleaner.assignedZone}) dengan skor ${String.format(Locale.US, "%.2f", bestOfficer.avgScore)}. ")
            }
            if (summary.unresolvedAlerts > 0) {
                append("Terdapat ${summary.unresolvedAlerts} laporan Cukup/Kurang yang masih menunggu tindak lanjut.")
            } else {
                append("Seluruh laporan kendala kebersihan telah tertangani dengan baik.")
            }
        }

        val hotspotTitle = if (topComplaintRoom != null) {
            "Area Prioritas: ${topComplaintRoom.key} (${topComplaintRoom.value.size} catatan)"
        } else {
            "Tidak Ada Area Keluhan Berulang"
        }

        val hotspotDetail = if (topComplaintRoom != null && worstOfficer != null) {
            "Zona ${worstOfficer.cleaner.assignedZone} (Petugas: ${worstOfficer.cleaner.name}) mencatat rasio keluhan terbanyak, terutama pada jam setelah istirahat siang. Pola komentar menunjukkan kebutuhan pengecekan ulang fasilitas umum & isi ulang perlengkapan sanitasi."
        } else {
            "Seluruh zona berada dalam kondisi bersih dan terkendali tanpa indikasi keluhan berulang."
        }

        val recommendations = buildList {
            if (topComplaintRoom != null) {
                add("Jadwalkan inspeksi tambahan pukul 12.30 WIB khusus di ${topComplaintRoom.key} untuk mencegah bau dan lantai basah.")
            }
            if (summary.unresolvedAlerts > 0) {
                add("Instruksikan tindak lanjut segera untuk ${summary.unresolvedAlerts} laporan aktif melalui menu Petugas Kebersihan.")
            }
            add("Pasang checklist isi ulang sabun cuci tangan dan tisu pada pintu dalam Toilet Lt. 1 & Lt. 2.")
            if (bestOfficer != null) {
                add("Jadikan standar kerja ${bestOfficer.cleaner.name} (Zona ${bestOfficer.cleaner.assignedZone}) sebagai acuan apresiasi bulanan pegawai non-ASN.")
            }
        }

        val monthlyOfficerNotes = officers.map { perf ->
            val predicate = when {
                perf.avgScore >= 2.75f -> "Sangat Baik (Direkomendasikan Apresiasi)"
                perf.avgScore >= 2.25f -> "Baik (Pertahankan Konsistensi)"
                else -> "Perlu Pembinaan & Penyesuaian Jadwal"
            }
            "${perf.cleaner.name} • Zona ${perf.cleaner.assignedZone}: Skor ${String.format(Locale.US, "%.2f", perf.avgScore)}/3.00 (${perf.puasCount} Puas, ${perf.cukupCount} Cukup, ${perf.kurangCount} Kurang) — $predicate"
        }

        val shareableText = buildString {
            appendLine("=== RAPOR EKSEKUTIF SILENI ===")
            appendLine("Dinas Perindustrian dan Perdagangan Kab. Sidoarjo")
            appendLine("Dicetak: $nowLabel [Analisis Sampel Lokal]")
            appendLine()
            appendLine("Ringkasan: $executiveSummary")
            appendLine()
            appendLine("Area Prioritas: $hotspotTitle")
            appendLine(hotspotDetail)
            appendLine()
            appendLine("Rapor 3 Petugas Kebersihan:")
            monthlyOfficerNotes.forEach { appendLine("- $it") }
            appendLine()
            appendLine("Rekomendasi Kasubbag Umum:")
            recommendations.forEachIndexed { idx, rec -> appendLine("${idx + 1}. $rec") }
        }

        return AiInsightReport(
            sampleLabel = "Sampel Analisis Lokal (Non-Firebase)",
            generatedAtLabel = nowLabel,
            executiveSummary = executiveSummary,
            hotspotZoneTitle = hotspotTitle,
            hotspotDetail = hotspotDetail,
            keywordInsights = keywordInsights,
            recommendations = recommendations,
            monthlyOfficerNotes = monthlyOfficerNotes,
            shareableReportText = shareableText,
        )
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.FeedbackEntity
import com.example.ui.components.OfficerComparisonBars
import com.example.ui.components.RatingDistributionDonut
import com.example.ui.components.RatingStatusBadge
import com.example.ui.components.ResolvedChip
import com.example.ui.components.SatisfactionTrendChart
import com.example.ui.components.ZoneBadge
import com.example.ui.state.SileniUiState
import com.example.ui.state.UserPersona
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalSileniColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    state: SileniUiState,
    onSelectPersona: (UserPersona) -> Unit,
    onSetFeedbackFilter: (Int?) -> Unit,
    onResolveFeedback: (Long, Boolean) -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToQr: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = state.dashboardSummary
    val ext = LocalSileniColors.current
    val filteredFeedbacks = remember(state.feedbacks, state.selectedFeedbackFilter) {
        val f = state.selectedFeedbackFilter
        if (f == null) state.feedbacks else state.feedbacks.filter { it.rating == f }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Civic Hero Card with Disperindag Sidoarjo Architectural Backdrop
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ext.navyHeroStart),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_disperindag),
                        contentDescription = "Gedung Disperindag Kabupaten Sidoarjo",
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(22.dp)),
                        contentScale = ContentScale.Crop,
                        alpha = 0.24f,
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        ext.navyHeroEnd.copy(alpha = 0.94f),
                                        ext.navyHeroStart.copy(alpha = 0.80f),
                                    ),
                                ),
                            ),
                    )
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.14f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFF7ADBBF),
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = "DISPERINDAG KAB. SIDOARJO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            Text(
                                text = state.activePersona.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFBFD7FF),
                            )
                        }

                        Text(
                            text = "SILENI • Monitoring Kebersihan",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                        )
                        Text(
                            text = "Evaluasi kinerja real-time 3 petugas kebersihan berbasis umpan balik QR Code ruangan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.86f),
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 2.dp),
                        ) {
                            Button(
                                onClick = onNavigateToFeedback,
                                modifier = Modifier.testTag("hero_scan_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ext.puas,
                                    contentColor = Color.White,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Simulasi Scan QR")
                            }
                            OutlinedButton(
                                onClick = onNavigateToQr,
                                modifier = Modifier.testTag("hero_qr_manager_button"),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            ) {
                                Text("Kelola QR")
                            }
                        }
                    }
                }
            }
        }

        // Persona Switcher Strip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Simulasi Hak Akses Pengguna (Persona)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    UserPersona.entries.forEach { persona ->
                        FilterChip(
                            selected = state.activePersona == persona,
                            onClick = { onSelectPersona(persona) },
                            modifier = Modifier.testTag("persona_chip_${persona.name.lowercase()}"),
                            label = { Text(persona.displayName) },
                        )
                    }
                }
            }
        }

        // 2x2 KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KpiStatCard(
                        title = "Skor Rata-Rata",
                        value = String.format(Locale.US, "%.2f", summary.averageScore),
                        subtitle = "dari skala 3.00",
                        icon = Icons.Rounded.Star,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    KpiStatCard(
                        title = "Tingkat Puas",
                        value = "${summary.satisfactionPercent}%",
                        subtitle = "${summary.puasCount} dari ${summary.totalReviews} ulasan",
                        icon = Icons.Rounded.CheckCircle,
                        accentColor = ext.puas,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KpiStatCard(
                        title = "Total Penilaian",
                        value = "${summary.totalReviews}",
                        subtitle = "${state.rooms.size} zona ruangan aktif",
                        icon = Icons.Rounded.Assessment,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    KpiStatCard(
                        title = "Perlu Tindak Lanjut",
                        value = "${summary.unresolvedAlerts}",
                        subtitle = if (summary.unresolvedAlerts > 0) "Keluhan belum selesai" else "Semua zona aman",
                        icon = Icons.Rounded.NotificationsActive,
                        accentColor = if (summary.unresolvedAlerts > 0) ext.kurang else ext.puas,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 7-Day Satisfaction Trend Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Tren Kepuasan Kebersihan (7 Hari)",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Skor harian skala 1.0 (Kurang) – 3.0 (Puas)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SatisfactionTrendChart(points = summary.dailyTrend)
                }
            }
        }

        // 3 Officers Comparison + Distribution Donut
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Perbandingan Kinerja 3 Petugas Kebersihan",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    OfficerComparisonBars(performances = state.officerPerformances)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Distribusi Penilaian Ruangan",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    RatingDistributionDonut(
                        puas = summary.puasCount,
                        cukup = summary.cukupCount,
                        kurang = summary.kurangCount,
                        satisfactionPercent = summary.satisfactionPercent,
                    )
                }
            }
        }

        // Recent Feedback Stream with Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Umpan Balik Terbaru Pengguna Ruangan",
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val filters = listOf<Pair<Int?, String>>(
                        null to "Semua (${state.feedbacks.size})",
                        3 to "Puas (${summary.puasCount})",
                        2 to "Cukup (${summary.cukupCount})",
                        1 to "Kurang (${summary.kurangCount})",
                    )
                    filters.forEach { (ratingKey, label) ->
                        FilterChip(
                            selected = state.selectedFeedbackFilter == ratingKey,
                            onClick = { onSetFeedbackFilter(ratingKey) },
                            modifier = Modifier.testTag("feedback_filter_${ratingKey ?: "all"}"),
                            label = { Text(label) },
                        )
                    }
                }
            }
        }

        items(filteredFeedbacks, key = { it.id }) { item ->
            FeedbackItemCard(
                feedback = item,
                canResolve = state.activePersona != UserPersona.PUBLIK,
                onToggleResolve = { onResolveFeedback(item.id, !item.isResolved) },
            )
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun FeedbackItemCard(
    feedback: FeedbackEntity,
    canResolve: Boolean,
    onToggleResolve: () -> Unit,
) {
    val timeFormatted = remember(feedback.timestampMillis) {
        SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(feedback.timestampMillis))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    ZoneBadge(zone = feedback.zone)
                    Text(
                        text = feedback.roomName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RatingStatusBadge(rating = feedback.rating)
            }

            Text(
                text = feedback.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (feedback.tagsCsv.isNotBlank()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    feedback.tagsCsv.split(",").filter { it.isNotBlank() }.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = tag.trim(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${feedback.reporterRole} • $timeFormatted",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (feedback.rating < 3) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ResolvedChip(isResolved = feedback.isResolved)
                        if (canResolve) {
                            OutlinedButton(
                                onClick = onToggleResolve,
                                modifier = Modifier.testTag("resolve_feedback_${feedback.id}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = if (feedback.isResolved) "Buka" else "Tangani",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

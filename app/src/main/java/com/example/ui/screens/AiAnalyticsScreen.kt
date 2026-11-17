package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.FirebaseAiNoticeBanner
import com.example.ui.state.AiUiState
import com.example.ui.state.SileniUiState
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalSileniColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAnalyticsScreen(
    state: SileniUiState,
    onRefreshInsight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val ext = LocalSileniColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Analitik & Insight AI",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = "Rapor kinerja bulanan & deteksi keluhan berulang untuk Kasubbag Umum.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedButton(
                        onClick = onRefreshInsight,
                        modifier = Modifier.testTag("refresh_ai_insight_button"),
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Perbarui insight", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Perbarui")
                    }
                }
                FirebaseAiNoticeBanner()
            }
        }

        when (val ai = state.aiState) {
            is AiUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CircularProgressIndicator()
                            Text("Menyusun ringkasan analitik...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            is AiUiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    ) {
                        Text(
                            text = ai.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            is AiUiState.Success -> {
                val report = ai.report

                // Executive Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Ringkasan Eksekutif",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Text(
                                        text = report.sampleLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                            }
                            Text(
                                text = report.executiveSummary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "Diperbarui: ${report.generatedAtLabel}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Hotspot Alert Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ext.kurangContainer.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, ext.kurang.copy(alpha = 0.4f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ReportProblem,
                                    contentDescription = null,
                                    tint = ext.kurang,
                                )
                                Text(
                                    text = report.hotspotZoneTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ext.onKurangContainer,
                                )
                            }
                            Text(
                                text = report.hotspotDetail,
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.onKurangContainer,
                            )
                        }
                    }
                }

                // Keyword Sentiment Chips
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
                                text = "Analisis Sentimen & Kata Kunci Ulasan",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                report.keywordInsights.forEach { kw ->
                                    val (bg, fg) = when (kw.sentimentLabel) {
                                        "Positif" -> ext.puasContainer to ext.onPuasContainer
                                        "Perlu Perhatian" -> ext.cukupContainer to ext.onCukupContainer
                                        else -> ext.kurangContainer to ext.onKurangContainer
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = bg,
                                    ) {
                                        Text(
                                            text = "${kw.keyword} (${kw.count}x)",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = fg,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Actionable Recommendations for Kasubbag Umum
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = ext.cukup,
                                )
                                Text(
                                    text = "Rekomendasi Tindak Lanjut Kasubbag Umum",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            report.recommendations.forEachIndexed { idx, rec ->
                                Text(
                                    text = "${idx + 1}. $rec",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }

                // Monthly Officer Report & Share Button
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
                                text = "Rapor Evaluasi Kinerja 3 Petugas",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            report.monthlyOfficerNotes.forEach { note ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                ) {
                                    Text(
                                        text = note,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Rapor Evaluasi Kebersihan SILENI - Disperindag Sidoarjo")
                                        putExtra(Intent.EXTRA_TEXT, report.shareableReportText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Rapor ke Pimpinan"))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("share_executive_report_button"),
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Bagikan Rapor ke Pimpinan")
                            }
                        }
                    }
                }
            }
        }
    }
}

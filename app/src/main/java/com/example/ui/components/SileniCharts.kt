package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.state.DailyTrendPoint
import com.example.ui.state.OfficerPerformance
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalSileniColors
import java.util.Locale

@Composable
fun SatisfactionTrendChart(
    points: List<DailyTrendPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.primary
    val accentEmerald = LocalSileniColors.current.puas
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 12.dp, bottom = 12.dp, start = 6.dp, end = 6.dp),
            ) {
                val w = size.width
                val h = size.height
                val minScore = 1.0f
                val maxScore = 3.0f
                val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                // Horizontal reference lines for 3.0, 2.0, 1.0
                listOf(1.0f, 2.0f, 3.0f).forEach { level ->
                    val ratio = (level - minScore) / (maxScore - minScore)
                    val y = h - (ratio * h)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dash,
                    )
                }

                val stepX = if (points.size > 1) w / (points.size - 1) else w
                val coords = points.mapIndexed { index, pt ->
                    val x = index * stepX
                    val clamped = pt.avgScore.coerceIn(minScore, maxScore)
                    val ratio = (clamped - minScore) / (maxScore - minScore)
                    val y = h - (ratio * h)
                    Offset(x, y)
                }

                // Area gradient fill
                val areaPath = Path().apply {
                    moveTo(coords.first().x, h)
                    coords.forEach { lineTo(it.x, it.y) }
                    lineTo(coords.last().x, h)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.24f),
                            lineColor.copy(alpha = 0.02f),
                        ),
                    ),
                )

                // Connecting line
                val linePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        val prev = coords[i - 1]
                        val curr = coords[i]
                        val midX = (prev.x + curr.x) / 2f
                        cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )

                // Data points
                coords.forEachIndexed { idx, pt ->
                    val dotColor = if (points[idx].avgScore >= 2.5f) accentEmerald else lineColor
                    drawCircle(
                        color = surfaceColor,
                        radius = 6.dp.toPx(),
                        center = pt,
                    )
                    drawCircle(
                        color = dotColor,
                        radius = 4.dp.toPx(),
                        center = pt,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            points.forEach { pt ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", pt.avgScore),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
fun OfficerComparisonBars(
    performances: List<OfficerPerformance>,
    modifier: Modifier = Modifier,
) {
    val ext = LocalSileniColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        performances.forEach { perf ->
            val targetProgress = (perf.avgScore / 3.0f).coerceIn(0.08f, 1f)
            val animatedProgress by animateFloatAsState(
                targetValue = targetProgress,
                animationSpec = tween(durationMillis = 550),
                label = "officerBarAnim",
            )
            val barColor = when {
                perf.avgScore >= 2.65f -> ext.puas
                perf.avgScore >= 2.10f -> ext.cukup
                else -> ext.kurang
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
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
                        ZoneBadge(zone = perf.cleaner.assignedZone)
                        Text(
                            text = perf.cleaner.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.2f", perf.avgScore)} / 3.0 (${perf.totalReviews} ulasan)",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                        color = barColor,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(barColor),
                    )
                }
            }
        }
    }
}

@Composable
fun RatingDistributionDonut(
    puas: Int,
    cukup: Int,
    kurang: Int,
    satisfactionPercent: Int,
    modifier: Modifier = Modifier,
) {
    val ext = LocalSileniColors.current
    val total = (puas + cukup + kurang).coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier.size(116.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(116.dp)) {
                val strokeWidth = 16.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                val segments = listOf(
                    puas.toFloat() to ext.puas,
                    cukup.toFloat() to ext.cukup,
                    kurang.toFloat() to ext.kurang,
                )

                var startAngle = -90f
                segments.forEach { (value, color) ->
                    val sweep = (value / total) * 360f
                    if (sweep > 0f) {
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = (sweep - 3f).coerceAtLeast(1f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                        startAngle += sweep
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$satisfactionPercent%",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Puas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LegendRow("Puas (Skor 3)", puas, ext.puas)
            LegendRow("Cukup (Skor 2)", cukup, ext.cukup)
            LegendRow("Kurang (Skor 1)", kurang, ext.kurang)
        }
    }
}

@Composable
private fun LegendRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
            fontWeight = FontWeight.Bold,
        )
    }
}

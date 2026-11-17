package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ChecklistTaskEntity
import com.example.data.local.FeedbackEntity
import com.example.data.local.RoomEntity
import com.example.ui.components.ZoneBadge
import com.example.ui.state.OfficerPerformance
import com.example.ui.state.SileniUiState
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalSileniColors
import java.util.Locale

@Composable
fun OfficersScreen(
    state: SileniUiState,
    onToggleTask: (Long, Boolean) -> Unit,
    onResolveFeedback: (Long, Boolean) -> Unit,
    onReassignRoomZone: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var roomPendingReassign by remember { mutableStateOf<RoomEntity?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Manajemen 3 Petugas Kebersihan",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Pantau zona tugas, checklist harian, dan notifikasi tindak lanjut keluhan per petugas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(state.officerPerformances, key = { it.cleaner.id }) { perf ->
            val zoneTasks = state.checklistTasks.filter { it.zone == perf.cleaner.assignedZone }
            val activeZoneAlerts = state.feedbacks.filter {
                it.zone == perf.cleaner.assignedZone && it.rating < 3 && !it.isResolved
            }

            OfficerDetailCard(
                performance = perf,
                tasks = zoneTasks,
                activeAlerts = activeZoneAlerts,
                onToggleTask = onToggleTask,
                onResolveAlert = { feedbackId -> onResolveFeedback(feedbackId, true) },
                onRequestReassignRoom = { room -> roomPendingReassign = room },
            )
        }
    }

    val selectedRoom = roomPendingReassign
    if (selectedRoom != null) {
        ReassignRoomDialog(
            room = selectedRoom,
            onDismiss = { roomPendingReassign = null },
            onConfirmZone = { newZone ->
                onReassignRoomZone(selectedRoom.id, newZone)
                roomPendingReassign = null
            },
        )
    }
}

@Composable
private fun OfficerDetailCard(
    performance: OfficerPerformance,
    tasks: List<ChecklistTaskEntity>,
    activeAlerts: List<FeedbackEntity>,
    onToggleTask: (Long, Boolean) -> Unit,
    onResolveAlert: (Long) -> Unit,
    onRequestReassignRoom: (RoomEntity) -> Unit,
) {
    val cleaner = performance.cleaner
    val ext = LocalSileniColors.current
    val scoreColor = when {
        performance.avgScore >= 2.65f -> ext.puas
        performance.avgScore >= 2.10f -> ext.cukup
        else -> ext.kurang
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header: Avatar, Name, Zone, Average Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = cleaner.initials,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = cleaner.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = cleaner.zoneTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = "${cleaner.shiftHours} • ${cleaner.employeeCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scoreColor.copy(alpha = 0.14f),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f", performance.avgScore),
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                            fontWeight = FontWeight.Bold,
                            color = scoreColor,
                        )
                        Text(
                            text = "${performance.satisfactionPercent}% Puas",
                            style = MaterialTheme.typography.labelSmall,
                            color = scoreColor,
                        )
                    }
                }
            }

            // Active Complaint Alert Banner for this officer
            if (activeAlerts.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ext.kurangContainer,
                    border = BorderStroke(1.dp, ext.kurang.copy(alpha = 0.35f)),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = ext.onKurangContainer,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "${activeAlerts.size} Notifikasi Perlu Tindak Lanjut",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ext.onKurangContainer,
                            )
                        }
                        activeAlerts.forEach { alert ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alert.roomName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ext.onKurangContainer,
                                    )
                                    Text(
                                        text = alert.comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ext.onKurangContainer.copy(alpha = 0.9f),
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { onResolveAlert(alert.id) },
                                    modifier = Modifier.testTag("officer_resolve_${alert.id}"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Selesai", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

            // Assigned Rooms List with Reassign button
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Zona Ruangan Tanggung Jawab (${performance.assignedRooms.size} ruang)",
                    style = MaterialTheme.typography.labelLarge,
                )
                performance.assignedRooms.forEach { room ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${room.id} • ${room.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "${room.floor} • ${room.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            OutlinedButton(
                                onClick = { onRequestReassignRoom(room) },
                                modifier = Modifier.testTag("reassign_room_${room.id}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = "Pindah zona ruangan",
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Pindah Zona", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

            // Interactive Daily Checklist
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Checklist Tugas Harian",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Text(
                        text = "${performance.completedTasks}/${performance.totalTasks} selesai",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleTask(task.id, !task.isCompleted) }
                            .padding(vertical = 2.dp)
                            .testTag("task_row_${task.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task.id, it) },
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            Text(
                                text = "Jadwal: ${task.timeSlot}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReassignRoomDialog(
    room: RoomEntity,
    onDismiss: () -> Unit,
    onConfirmZone: (String) -> Unit,
) {
    var selectedZone by remember(room) { mutableStateOf(room.zone) }
    val zones = listOf(
        "A" to "Zona A • Budi Santoso (Pimpinan & Rapat)",
        "B" to "Zona B • Siti Aminah (Pelayanan Publik & Lobby)",
        "C" to "Zona C • Agus Prasetyo (Bidang Teknis & Fasum)",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pindah Zona Penugasan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ZoneBadge(zone = room.zone)
                    Text(room.name, style = MaterialTheme.typography.titleSmall)
                }
                zones.forEach { (zoneCode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedZone = zoneCode }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedZone == zoneCode,
                            onClick = { selectedZone = zoneCode },
                        )
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmZone(selectedZone) },
                modifier = Modifier.testTag("confirm_reassign_button"),
            ) {
                Text("Simpan Zona")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
    )
}

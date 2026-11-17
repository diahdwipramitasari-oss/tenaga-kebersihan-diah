package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.RoomEntity
import com.example.ui.components.ZoneBadge
import com.example.ui.state.SileniUiState
import com.example.ui.theme.LocalSileniColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicFeedbackScreen(
    state: SileniUiState,
    onSelectRoom: (String) -> Unit,
    onSubmitFeedback: (RoomEntity, Int, List<String>, String, String) -> Unit,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedRoom = state.rooms.firstOrNull { it.id == state.selectedRoomIdForFeedback }
        ?: state.rooms.firstOrNull()
    val assignedCleaner = selectedRoom?.let { r ->
        state.cleaners.firstOrNull { it.assignedZone == r.zone }
    }

    var selectedRating by remember { mutableIntStateOf(3) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var commentText by remember { mutableStateOf("") }
    var reporterRole by remember { mutableStateOf("Pegawai Internal") }
    var submittedSuccessfully by remember { mutableStateOf(false) }

    val availableTags = remember(selectedRating) {
        if (selectedRating == 3) {
            listOf("Lantai Bersih", "Rapi & Harum", "Wastafel Kering", "Nyaman")
        } else {
            listOf("Lantai Kotor", "Bau Kurang Sedap", "Tempat Sampah Penuh", "Meja Berdebu", "Tisu/Sabun Habis")
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AnimatedContent(
                    targetState = submittedSuccessfully,
                    label = "feedbackSubmitTransition",
                ) { isDone ->
                    if (isDone) {
                        SuccessConfirmationCard(
                            roomName = selectedRoom?.name ?: "Ruangan",
                            onSubmitAnother = {
                                selectedRating = 3
                                selectedTags = emptySet()
                                commentText = ""
                                submittedSuccessfully = false
                            },
                            onBackToDashboard = onBackToDashboard,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // QR Context Header
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
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
                                            imageVector = Icons.Rounded.QrCodeScanner,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = "Form Penilaian Publik (Scan QR)",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    Text(
                                        text = "Pilih ruangan yang sedang dinilai (simulasi hasil pindai stiker QR). Penilaian bersifat anonim.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        state.rooms.forEach { room ->
                                            FilterChip(
                                                selected = selectedRoom?.id == room.id,
                                                onClick = { onSelectRoom(room.id) },
                                                modifier = Modifier.testTag("select_room_chip_${room.id}"),
                                                label = { Text("${room.id} • ${room.name}") },
                                            )
                                        }
                                    }

                                    if (selectedRoom != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            ZoneBadge(zone = selectedRoom.zone)
                                            Text(
                                                text = "${selectedRoom.floor} • Petugas: ${assignedCleaner?.name ?: "-"}",
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                        }
                                    }
                                }
                            }

                            // 3 Large Emoji Rating Selector
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Bagaimana kondisi kebersihan ruangan ini?",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    val ext = LocalSileniColors.current
                                    EmojiRatingOption(
                                        title = "Puas",
                                        subtitle = "Bersih & Rapi",
                                        icon = Icons.Rounded.SentimentVerySatisfied,
                                        selected = selectedRating == 3,
                                        accentColor = ext.puas,
                                        containerColor = ext.puasContainer,
                                        onClick = {
                                            selectedRating = 3
                                            selectedTags = emptySet()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("rating_option_puas"),
                                    )
                                    EmojiRatingOption(
                                        title = "Cukup",
                                        subtitle = "Perlu Cek Ulang",
                                        icon = Icons.Rounded.SentimentNeutral,
                                        selected = selectedRating == 2,
                                        accentColor = ext.cukup,
                                        containerColor = ext.cukupContainer,
                                        onClick = {
                                            selectedRating = 2
                                            selectedTags = emptySet()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("rating_option_cukup"),
                                    )
                                    EmojiRatingOption(
                                        title = "Kurang",
                                        subtitle = "Segera Tangani",
                                        icon = Icons.Rounded.SentimentDissatisfied,
                                        selected = selectedRating == 1,
                                        accentColor = ext.kurang,
                                        containerColor = ext.kurangContainer,
                                        onClick = {
                                            selectedRating = 1
                                            selectedTags = emptySet()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("rating_option_kurang"),
                                    )
                                }
                            }

                            // Quick Tag Chips
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Kategori Kondisi (Opsional)",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    availableTags.forEach { tag ->
                                        val isPicked = tag in selectedTags
                                        FilterChip(
                                            selected = isPicked,
                                            onClick = {
                                                selectedTags = if (isPicked) selectedTags - tag else selectedTags + tag
                                            },
                                            label = { Text(tag) },
                                        )
                                    }
                                }
                            }

                            // Reporter Type & Comment
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Status Pengisi Penilaian",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Pegawai Internal", "Tamu Dinas").forEach { role ->
                                        FilterChip(
                                            selected = reporterRole == role,
                                            onClick = { reporterRole = role },
                                            label = { Text(role) },
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    label = { Text("Catatan atau kendala singkat (opsional)") },
                                    placeholder = { Text("Contoh: Lantai bersih, atau wastafel butuh sabun...") },
                                    minLines = 3,
                                    maxLines = 5,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("feedback_comment_input"),
                                )
                            }

                            Button(
                                onClick = {
                                    if (selectedRoom != null) {
                                        onSubmitFeedback(
                                            selectedRoom,
                                            selectedRating,
                                            selectedTags.toList(),
                                            commentText,
                                            reporterRole,
                                        )
                                        submittedSuccessfully = true
                                    }
                                },
                                enabled = selectedRoom != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_feedback_button"),
                            ) {
                                Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Kirim Penilaian Sekarang")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiRatingOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "ratingScale",
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) containerColor else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accentColor else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SuccessConfirmationCard(
    roomName: String,
    onSubmitAnother: () -> Unit,
    onBackToDashboard: () -> Unit,
) {
    val ext = LocalSileniColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ext.puasContainer.copy(alpha = 0.55f)),
        border = BorderStroke(1.5.dp, ext.puas),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Berhasil dikirim",
                tint = ext.puas,
                modifier = Modifier.size(56.dp),
            )
            Text(
                text = stringResource(R.string.feedback_success_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Penilaian untuk $roomName telah diterima. ${stringResource(R.string.feedback_success_subtitle)}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                OutlinedButton(
                    onClick = onSubmitAnother,
                    modifier = Modifier.testTag("submit_another_button"),
                ) {
                    Text("Nilai Ruangan Lain")
                }
                Button(
                    onClick = onBackToDashboard,
                    modifier = Modifier.testTag("back_to_dashboard_button"),
                ) {
                    Text("Lihat Dashboard")
                }
            }
        }
    }
}

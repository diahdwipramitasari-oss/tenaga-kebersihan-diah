package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.CleanerEntity
import com.example.data.local.RoomEntity
import com.example.ui.components.QrCodeImage
import com.example.ui.components.ZoneBadge
import com.example.ui.state.SileniUiState
import com.example.ui.theme.JetBrainsMono

@Composable
fun QrManagerScreen(
    state: SileniUiState,
    onAddRoom: (roomId: String, name: String, floor: String, zone: String, category: String) -> Unit,
    onSimulateScanRoom: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedZoneFilter by remember { mutableStateOf<String?>(null) }
    var previewRoom by remember { mutableStateOf<RoomEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val visibleRooms = remember(state.rooms, selectedZoneFilter) {
        if (selectedZoneFilter == null) state.rooms
        else state.rooms.filter { it.zone == selectedZoneFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_room_fab"),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Tambah Ruangan") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "QR Code Manager Ruangan",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Ketuk ruangan untuk melihat stiker QR siap cetak atau uji coba scan langsung ke Form Penilaian Publik.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        listOf(null to "Semua", "A" to "Zona A", "B" to "Zona B", "C" to "Zona C").forEach { (z, label) ->
                            FilterChip(
                                selected = selectedZoneFilter == z,
                                onClick = { selectedZoneFilter = z },
                                label = { Text(label) },
                            )
                        }
                    }
                }
            }

            items(visibleRooms, key = { it.id }) { room ->
                val cleaner = state.cleaners.firstOrNull { it.assignedZone == room.zone }
                RoomQrCard(
                    room = room,
                    cleaner = cleaner,
                    onPreviewSticker = { previewRoom = room },
                    onSimulateScan = { onSimulateScanRoom(room.id) },
                )
            }
        }
    }

    val roomForDialog = previewRoom
    if (roomForDialog != null) {
        val cleaner = state.cleaners.firstOrNull { it.assignedZone == roomForDialog.zone }
        QrStickerPreviewDialog(
            room = roomForDialog,
            cleaner = cleaner,
            onDismiss = { previewRoom = null },
            onSimulateScan = {
                previewRoom = null
                onSimulateScanRoom(roomForDialog.id)
            },
        )
    }

    if (showAddDialog) {
        AddRoomDialog(
            nextIndex = state.rooms.size + 1,
            onDismiss = { showAddDialog = false },
            onSave = { id, name, floor, zone, category ->
                onAddRoom(id, name, floor, zone, category)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun RoomQrCard(
    room: RoomEntity,
    cleaner: CleanerEntity?,
    onPreviewSticker: () -> Unit,
    onSimulateScan: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreviewSticker() }
            .testTag("qr_room_card_${room.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            QrCodeImage(
                payload = room.qrPayload,
                contentDescription = "QR Code ${room.name}",
                sizeDp = 78.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = room.id,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    ZoneBadge(zone = room.zone)
                }
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "${room.floor} • ${room.category} • Petugas: ${cleaner?.name ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    OutlinedButton(
                        onClick = onPreviewSticker,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCode2,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Stiker QR", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = onSimulateScan,
                        modifier = Modifier.testTag("simulate_scan_${room.id}"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Simulasi Scan", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun QrStickerPreviewDialog(
    room: RoomEntity,
    cleaner: CleanerEntity?,
    onDismiss: () -> Unit,
    onSimulateScan: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pratinjau Stiker QR Ruangan",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "SILENI • DISPERINDAG SIDOARJO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    QrCodeImage(
                        payload = room.qrPayload,
                        contentDescription = "Stiker QR ${room.name}",
                        sizeDp = 185.dp,
                    )
                    Text(
                        text = "Pindai untuk menilai kebersihan ruangan secara anonim",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider()
                    Text(
                        text = "Kode: ${room.id} • Zona ${room.zone} (${cleaner?.name ?: "-"})",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSimulateScan,
                modifier = Modifier.testTag("dialog_simulate_scan"),
            ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Buka Form Nilai")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Stiker QR SILENI Disperindag Sidoarjo\nRuangan: ${room.name} (${room.id})\nZona: ${room.zone} • Petugas: ${cleaner?.name ?: "-"}\nLink QR: ${room.qrPayload}",
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Detail QR Ruangan"))
                    },
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Bagikan")
                }
                TextButton(onClick = onDismiss) {
                    Text("Tutup")
                }
            }
        },
    )
}

@Composable
private fun AddRoomDialog(
    nextIndex: Int,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
) {
    var roomId by remember { mutableStateOf("R-${nextIndex.toString().padStart(2, '0')}") }
    var name by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("Lantai 1") }
    var zone by remember { mutableStateOf("B") }
    var category by remember { mutableStateOf("Pelayanan Publik") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Zona / Ruangan QR") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomId,
                    onValueChange = { roomId = it },
                    label = { Text("Kode Ruangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Ruangan (mis. Aula Disperindag)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_room_name_input"),
                )
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Lantai") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("A", "B", "C").forEach { z ->
                        FilterChip(
                            selected = zone == z,
                            onClick = { zone = z },
                            label = { Text("Zona $z") },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(roomId, name, floor, zone, category) },
                enabled = roomId.isNotBlank() && name.isNotBlank(),
                modifier = Modifier.testTag("save_new_room_button"),
            ) {
                Text("Generate QR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
    )
}

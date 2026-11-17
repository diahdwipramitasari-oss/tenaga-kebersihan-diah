package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.RoomEntity
import com.example.data.local.SileniDatabase
import com.example.data.repository.AiInsightRepository
import com.example.data.repository.PlaceholderAiInsightRepository
import com.example.data.repository.SileniRepository
import com.example.data.repository.SileniSnapshot
import com.example.ui.state.AiUiState
import com.example.ui.state.SileniUiState
import com.example.ui.state.UserPersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SileniViewModel(
    private val repository: SileniRepository,
    private val aiRepository: AiInsightRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SileniUiState())
    val uiState: StateFlow<SileniUiState> = _uiState.asStateFlow()

    private var latestSnapshot: SileniSnapshot? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            repository.observeSnapshot().collect { snapshot ->
                latestSnapshot = snapshot
                val currentSelected = _uiState.value.selectedRoomIdForFeedback
                val validRoomId = if (snapshot.rooms.any { it.id == currentSelected }) {
                    currentSelected
                } else {
                    snapshot.rooms.firstOrNull()?.id ?: "R-01"
                }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        cleaners = snapshot.cleaners,
                        rooms = snapshot.rooms,
                        feedbacks = snapshot.feedbacks,
                        checklistTasks = snapshot.tasks,
                        officerPerformances = snapshot.officerPerformances,
                        dashboardSummary = snapshot.dashboardSummary,
                        selectedRoomIdForFeedback = validRoomId,
                    )
                }
                refreshAiInsight(snapshot)
            }
        }
    }

    fun selectPersona(persona: UserPersona) {
        _uiState.update {
            it.copy(
                activePersona = persona,
                snackbarMessage = "Mode tampilan beralih ke ${persona.displayName}",
            )
        }
    }

    fun setFeedbackFilter(rating: Int?) {
        _uiState.update { it.copy(selectedFeedbackFilter = rating) }
    }

    fun selectRoomForFeedback(roomId: String) {
        _uiState.update { it.copy(selectedRoomIdForFeedback = roomId) }
    }

    fun submitFeedback(
        room: RoomEntity,
        rating: Int,
        tags: List<String>,
        comment: String,
        reporterRole: String,
    ) {
        viewModelScope.launch {
            repository.submitFeedback(room, rating, tags, comment, reporterRole)
            _uiState.update {
                it.copy(snackbarMessage = "Penilaian untuk ${room.name} tersimpan ke sistem.")
            }
        }
    }

    fun markFeedbackResolved(feedbackId: Long, resolved: Boolean) {
        viewModelScope.launch {
            repository.markFeedbackResolved(feedbackId, resolved)
            _uiState.update {
                it.copy(
                    snackbarMessage = if (resolved) "Laporan ditandai telah ditangani." else "Status laporan dibuka kembali."
                )
            }
        }
    }

    fun addRoom(
        roomId: String,
        name: String,
        floor: String,
        zone: String,
        category: String,
    ) {
        viewModelScope.launch {
            repository.addRoom(roomId, name, floor, zone, category)
            _uiState.update {
                it.copy(snackbarMessage = "Ruangan $name & QR Code berhasil ditambahkan.")
            }
        }
    }

    fun reassignRoomZone(roomId: String, newZone: String) {
        viewModelScope.launch {
            repository.updateRoomZone(roomId, newZone)
            _uiState.update {
                it.copy(snackbarMessage = "Zona penugasan ruangan $roomId diperbarui ke Zona $newZone.")
            }
        }
    }

    fun toggleTaskCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(taskId, completed)
        }
    }

    fun regenerateAiInsight() {
        val snapshot = latestSnapshot ?: return
        refreshAiInsight(snapshot)
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun refreshAiInsight(snapshot: SileniSnapshot) {
        viewModelScope.launch {
            _uiState.update { it.copy(aiState = AiUiState.Loading) }
            try {
                val report = aiRepository.generateExecutiveInsight(snapshot)
                _uiState.update { it.copy(aiState = AiUiState.Success(report)) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(aiState = AiUiState.Error(e.localizedMessage ?: "Gagal memproses analisis."))
                }
            }
        }
    }

    companion object {
        fun provideFactory(appContext: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = SileniDatabase.getInstance(appContext)
                val repo = SileniRepository(db.sileniDao())
                val aiRepo = PlaceholderAiInsightRepository()
                SileniViewModel(repo, aiRepo)
            }
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = SileniDatabase.getInstance(app.applicationContext)
                val repo = SileniRepository(db.sileniDao())
                val aiRepo = PlaceholderAiInsightRepository()
                SileniViewModel(repo, aiRepo)
            }
        }
    }
}

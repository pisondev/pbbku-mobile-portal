package id.pbbku.mobileportal.feature.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.local.report.ReportDraftEntity
import id.pbbku.mobileportal.data.local.report.ReportDraftStatus
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportDraftViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val app = application as PbbKuApplication
    private val nikScopedDemoRepository = app.nikScopedDemoRepository
    private val reportDraftRepository = app.reportDraftRepository
    private val _uiState = MutableStateFlow(ReportDraftUiState())

    val uiState: StateFlow<ReportDraftUiState> = _uiState.asStateFlow()

    fun load(nopDisplay: String, noBng: String?) {
        val nop = Nop.parseOrNull(nopDisplay)
        if (nop == null) {
            _uiState.value = ReportDraftUiState(
                buildingMessage = "Format NOP tidak valid.",
            )
            return
        }

        val cleanNoBng = noBng.orEmpty()
        _uiState.update {
            it.copy(
                nop = nop,
                noBng = cleanNoBng,
                isAccessAllowed = true,
                buildingMessage = null,
                saveMessage = null,
            )
        }
        viewModelScope.launch {
            if (!nikScopedDemoRepository.canAccess(nop)) {
                _uiState.update {
                    it.copy(
                        isAccessAllowed = false,
                        buildingMessage = "Akses ditolak. Laporan hanya dapat dibuat untuk NOP yang terhubung dengan NIK login.",
                    )
                }
                return@launch
            }
            loadBuildingListAndSelection(nop, cleanNoBng)
        }
    }

    fun onNoBngChange(value: String) {
        val selectedNoBng = value.filter(Char::isDigit)
        _uiState.update {
            it.copy(
                noBng = selectedNoBng,
                oldBuildingDetail = null,
                oldBuildingAreaText = "",
                oldFloorCountText = "",
                newBuildingAreaText = "",
                newFloorCountText = "",
                description = "",
                status = ReportDraftStatus.DRAFT,
                validation = ReportDraftValidationResult(),
                saveMessage = null,
                showSummary = false,
            )
        }
        val nop = _uiState.value.nop ?: return
        viewModelScope.launch {
            loadExistingDraft(nop, selectedNoBng)
            loadBuildingDetailIfAvailable(nop, selectedNoBng)
        }
    }

    fun onChangeTypeChange(value: String) {
        _uiState.update {
            it.copy(
                changeType = value,
                validation = ReportDraftValidationResult(),
                saveMessage = null,
                showSummary = false,
            )
        }
    }

    fun onNewBuildingAreaChange(value: String) {
        _uiState.update {
            it.copy(
                newBuildingAreaText = value,
                validation = it.validation.copy(newBuildingAreaError = null),
                saveMessage = null,
                showSummary = false,
            )
        }
    }

    fun onNewFloorCountChange(value: String) {
        _uiState.update {
            it.copy(
                newFloorCountText = value.filter { char -> char.isDigit() },
                validation = it.validation.copy(newFloorCountError = null),
                saveMessage = null,
                showSummary = false,
            )
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update {
            it.copy(
                description = value,
                validation = it.validation.copy(descriptionError = null),
                saveMessage = null,
                showSummary = false,
            )
        }
    }

    fun saveDraft() {
        save(status = ReportDraftStatus.DRAFT, requireDescription = false)
    }

    fun prepareSummary() {
        save(status = ReportDraftStatus.READY_TO_SUBMIT, requireDescription = true)
    }

    fun sendSimulation() {
        save(status = ReportDraftStatus.SENT_SIMULATION, requireDescription = true)
    }

    fun deleteDraft() {
        val state = _uiState.value
        val nop = state.nop ?: return
        if (!state.isAccessAllowed) {
            _uiState.update { it.copy(saveMessage = "Akses ditolak untuk NOP ini.") }
            return
        }
        viewModelScope.launch {
            if (!nikScopedDemoRepository.canAccess(nop)) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        saveMessage = "Akses ditolak untuk NOP ini.",
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(isDeleting = true) }
            reportDraftRepository.deleteById(draftId(nop, state.noBng))
            _uiState.update {
                it.copy(
                    isDeleting = false,
                    status = ReportDraftStatus.DRAFT,
                    newBuildingAreaText = "",
                    newFloorCountText = "",
                    description = "",
                    saveMessage = "Draft lokal dihapus.",
                    showSummary = false,
                )
            }
        }
    }

    private suspend fun loadExistingDraft(nop: Nop, noBng: String) {
        val draft = reportDraftRepository.getById(draftId(nop, noBng)) ?: return
        _uiState.update {
            it.copy(
                changeType = draft.changeType,
                oldBuildingAreaText = draft.oldBuildingArea?.toCleanText().orEmpty(),
                newBuildingAreaText = draft.newBuildingArea?.toCleanText().orEmpty(),
                oldFloorCountText = draft.oldFloorCount?.toString().orEmpty(),
                newFloorCountText = draft.newFloorCount?.toString().orEmpty(),
                description = draft.description,
                status = draft.status,
                showSummary = draft.status != ReportDraftStatus.DRAFT,
            )
        }
    }

    private suspend fun loadBuildingListAndSelection(nop: Nop, requestedNoBng: String) {
        when (val result = nikScopedDemoRepository.listBangunan(nop)) {
            AppResult.Empty -> {
                _uiState.update {
                    it.copy(
                        availableBuildings = emptyList(),
                        noBng = "",
                        buildingMessage = "NOP ini belum memiliki data bangunan. Laporan perubahan bangunan belum dapat dibuat.",
                    )
                }
            }
            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        availableBuildings = emptyList(),
                        buildingMessage = "${result.message} Daftar bangunan tidak dimuat.",
                    )
                }
            }
            AppResult.Loading -> Unit
            is AppResult.Success -> {
                val buildings = result.data
                val selectedNoBng = requestedNoBng
                    .takeIf { requested -> buildings.any { it.noBng == requested } }
                    ?: buildings.firstOrNull()?.noBng.orEmpty()
                _uiState.update {
                    it.copy(
                        availableBuildings = buildings,
                        noBng = selectedNoBng,
                        buildingMessage = if (buildings.isEmpty()) {
                            "NOP ini belum memiliki data bangunan. Laporan perubahan bangunan belum dapat dibuat."
                        } else {
                            null
                        },
                    )
                }
                if (selectedNoBng.isNotBlank()) {
                    loadExistingDraft(nop, selectedNoBng)
                    loadBuildingDetailIfAvailable(nop, selectedNoBng)
                }
            }
        }
    }

    private suspend fun loadBuildingDetailIfAvailable(nop: Nop, noBng: String) {
        if (noBng.isBlank()) {
            _uiState.update {
                it.copy(
                    buildingMessage = "Pilih bangunan yang akan diajukan perubahannya.",
                )
            }
            return
        }
        if (noBng.toIntOrNull() == null) {
            _uiState.update { it.copy(buildingMessage = "Nomor bangunan tidak valid.") }
            return
        }

        _uiState.update { it.copy(isLoadingBuilding = true, buildingMessage = null) }
        when (val result = nikScopedDemoRepository.getBuilding(nop, noBng)) {
            AppResult.Empty -> {
                _uiState.update {
                    it.copy(
                        isLoadingBuilding = false,
                        buildingMessage = "Data lama LSPOP tidak tersedia. Form tetap dapat disimpan sebagai draft lokal.",
                    )
                }
            }
            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoadingBuilding = false,
                        buildingMessage = "${result.message} Data lama tidak dimuat.",
                    )
                }
            }
            AppResult.Loading -> Unit
            is AppResult.Success -> {
                val detail = result.data
                _uiState.update {
                    it.copy(
                        isLoadingBuilding = false,
                        oldBuildingDetail = detail,
                        oldBuildingAreaText = detail.luasBangunan?.toCleanText().orEmpty(),
                        oldFloorCountText = detail.jumlahLantai?.toString().orEmpty(),
                        buildingMessage = null,
                    )
                }
            }
        }
    }

    private fun save(status: ReportDraftStatus, requireDescription: Boolean) {
        val state = _uiState.value
        val nop = state.nop ?: return
        if (!state.isAccessAllowed) {
            _uiState.update { it.copy(saveMessage = "Akses ditolak untuk NOP ini.") }
            return
        }
        val validation = ReportDraftFormValidator.validate(
            newBuildingAreaText = state.newBuildingAreaText,
            newFloorCountText = state.newFloorCountText,
            description = state.description,
            requireDescription = requireDescription,
            validateArea = state.showsAreaFields,
            validateFloor = state.showsFloorFields,
        )
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    validation = validation,
                    saveMessage = "Periksa kembali isian laporan.",
                    showSummary = false,
                )
            }
            return
        }

        val draft = ReportDraftEntity(
            id = draftId(nop, state.noBng),
            nopDisplay = nop.asDisplayText(),
            noBng = state.noBng.takeIf { it.isNotBlank() },
            changeType = state.changeType,
            oldBuildingArea = state.oldBuildingAreaText
                .takeIf { state.showsAreaFields }
                ?.let(ReportDraftFormValidator::parseOptionalDouble),
            newBuildingArea = state.newBuildingAreaText
                .takeIf { state.showsAreaFields }
                ?.let(ReportDraftFormValidator::parseOptionalDouble),
            oldFloorCount = state.oldFloorCountText
                .takeIf { state.showsFloorFields }
                ?.let(ReportDraftFormValidator::parseOptionalInt),
            newFloorCount = state.newFloorCountText
                .takeIf { state.showsFloorFields }
                ?.let(ReportDraftFormValidator::parseOptionalInt),
            description = state.description.trim(),
            status = status,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            if (!nikScopedDemoRepository.canAccess(nop)) {
                _uiState.update {
                    it.copy(
                        saveMessage = "Akses ditolak untuk NOP ini.",
                        showSummary = false,
                    )
                }
                return@launch
            }
            reportDraftRepository.saveDraft(draft)
            _uiState.update {
                it.copy(
                    status = status,
                    validation = ReportDraftValidationResult(),
                    saveMessage = status.toSaveMessage(),
                    showSummary = status != ReportDraftStatus.DRAFT,
                )
            }
        }
    }

    private fun draftId(nop: Nop, noBng: String): String {
        return "${nop.asDisplayText()}-${noBng.ifBlank { "manual" }}"
    }

    private fun ReportDraftStatus.toSaveMessage(): String {
        return when (this) {
            ReportDraftStatus.DRAFT -> "Draft permohonan disimpan."
            ReportDraftStatus.READY_TO_SUBMIT -> "Ringkasan permohonan siap diverifikasi."
            ReportDraftStatus.SENT_SIMULATION -> "Permohonan masuk ke antrean verifikasi."
        }
    }

    private fun Double.toCleanText(): String {
        val asLong = toLong()
        return if (this == asLong.toDouble()) asLong.toString() else toString()
    }
}

package id.pbbku.mobileportal.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.BuildConfig
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.data.api.SimpbbApiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val app = application as PbbKuApplication
    private val reminderRepository = app.paymentReminderRepository
    private val localCacheRepository = app.localCacheRepository
    private val reportDraftRepository = app.reportDraftRepository
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appVersionText = "PBB-Ku ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            debugModeEnabled = BuildConfig.DEBUG,
        ),
    )

    val reminderEnabled: StateFlow<Boolean> = reminderRepository.reminderEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            reminderRepository.setReminderEnabled(enabled)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            localCacheRepository.clearAll()
            _uiState.update {
                it.copy(cacheMessage = "Cache data terakhir berhasil dihapus.")
            }
        }
    }

    fun clearDrafts() {
        viewModelScope.launch {
            reportDraftRepository.clearAll()
            _uiState.update {
                it.copy(draftMessage = "Semua draft laporan lokal berhasil dihapus.")
            }
        }
    }

    fun debugSummary(): List<String> {
        if (!BuildConfig.DEBUG) return emptyList()
        return listOf(
            "Build: debug",
            "Base URL API: ${SimpbbApiConfig.BASE_URL}",
            "Log jaringan: BASIC tanpa body request/response",
            "Demo akses: objek, SPPT, pembayaran, dan laporan dibatasi oleh NIK session",
            "Data demo: NIK penuh disimpan lokal agar pembatasan session bisa diuji",
        )
    }
}

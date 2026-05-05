package id.pbbku.mobileportal.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val reminderRepository = (application as PbbKuApplication).paymentReminderRepository

    val reminderEnabled: StateFlow<Boolean> = reminderRepository.reminderEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            reminderRepository.setReminderEnabled(enabled)
        }
    }
}

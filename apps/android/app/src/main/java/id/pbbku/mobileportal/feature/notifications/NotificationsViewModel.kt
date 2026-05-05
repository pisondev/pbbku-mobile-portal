package id.pbbku.mobileportal.feature.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val reminderRepository = (application as PbbKuApplication).paymentReminderRepository

    val uiState: StateFlow<NotificationsUiState> = combine(
        reminderRepository.reminderEnabled,
        reminderRepository.reminders,
    ) { enabled, reminders ->
        NotificationsUiState(
            reminderEnabled = enabled,
            reminders = reminders,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotificationsUiState(),
    )

    fun showDemoReminder() {
        viewModelScope.launch {
            reminderRepository.ensureDemoReminderIfEmpty()
        }
    }
}

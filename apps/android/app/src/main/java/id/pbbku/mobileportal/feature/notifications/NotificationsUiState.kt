package id.pbbku.mobileportal.feature.notifications

import id.pbbku.mobileportal.domain.model.PaymentReminder

data class NotificationsUiState(
    val reminderEnabled: Boolean = false,
    val reminders: List<PaymentReminder> = emptyList(),
)

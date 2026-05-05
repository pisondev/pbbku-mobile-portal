package id.pbbku.mobileportal.feature.settings

data class SettingsUiState(
    val cacheMessage: String? = null,
    val draftMessage: String? = null,
    val appVersionText: String = "",
    val debugModeEnabled: Boolean = false,
)

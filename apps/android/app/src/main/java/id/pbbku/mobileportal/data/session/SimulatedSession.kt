package id.pbbku.mobileportal.data.session

data class SimulatedSession(
    val isLoggedIn: Boolean,
    val displayName: String?,
    val nik: String?,
    val maskedNik: String?,
    val sessionToken: String?,
    val createdAtEpochMillis: Long?,
) {
    companion object {
        val LoggedOut = SimulatedSession(
            isLoggedIn = false,
            displayName = null,
            nik = null,
            maskedNik = null,
            sessionToken = null,
            createdAtEpochMillis = null,
        )
    }
}

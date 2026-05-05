package id.pbbku.mobileportal.data.session

data class SimulatedSession(
    val isLoggedIn: Boolean,
    val maskedNik: String?,
    val sessionToken: String?,
    val createdAtEpochMillis: Long?,
) {
    companion object {
        val LoggedOut = SimulatedSession(
            isLoggedIn = false,
            maskedNik = null,
            sessionToken = null,
            createdAtEpochMillis = null,
        )
    }
}

package id.pbbku.mobileportal.feature.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.security.isValidNik
import id.pbbku.mobileportal.data.session.SimulatedSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val sessionDataStore = (application as PbbKuApplication).sessionDataStore
    private var pendingNik: String? = null

    val session: StateFlow<SimulatedSession?> = sessionDataStore.session.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun requestOtp(nik: String): Boolean {
        val normalizedNik = nik.trim()
        if (!normalizedNik.isValidNik()) return false
        pendingNik = normalizedNik
        return true
    }

    fun hasPendingNik(): Boolean = pendingNik != null

    fun verifyOtp(
        otp: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val nik = pendingNik
        if (nik == null || otp.trim() != DEMO_OTP) {
            onError()
            return
        }
        viewModelScope.launch {
            sessionDataStore.saveLogin(nik)
            pendingNik = null
            onSuccess()
        }
    }

    fun logout() {
        viewModelScope.launch {
            pendingNik = null
            sessionDataStore.logout()
        }
    }

    companion object {
        const val DEMO_OTP = "123456"
    }
}

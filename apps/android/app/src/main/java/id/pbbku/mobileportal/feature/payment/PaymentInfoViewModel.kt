package id.pbbku.mobileportal.feature.payment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentInfoViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val nikScopedDemoRepository = (application as PbbKuApplication).nikScopedDemoRepository
    private val _uiState = MutableStateFlow(PaymentInfoUiState())

    val uiState: StateFlow<PaymentInfoUiState> = _uiState.asStateFlow()

    fun load(nopDisplay: String, taxYearText: String) {
        val nop = Nop.parseOrNull(nopDisplay)
        val taxYear = taxYearText.toIntOrNull()
        if (nop == null) {
            _uiState.value = PaymentInfoUiState(errorMessage = "Format NOP tidak valid.")
            return
        }
        if (taxYear == null) {
            _uiState.value = PaymentInfoUiState(
                nop = nop,
                errorMessage = "Tahun pajak tidak valid.",
            )
            return
        }
        _uiState.update {
            it.copy(
                nop = nop,
                taxYear = taxYear,
                isLoading = true,
                isSimulatingPayment = false,
                detail = null,
                summary = null,
                paymentFlow = null,
                errorMessage = null,
                emptyMessage = null,
                actionMessage = null,
            )
        }
        viewModelScope.launch {
            loadPaymentFlow(nop, taxYear)
        }
    }

    fun retry() {
        val state = _uiState.value
        val nop = state.nop ?: return
        val taxYear = state.taxYear ?: return
        load(nop.asDisplayText(), taxYear.toString())
    }

    fun simulatePaymentSuccess() {
        val paymentId = _uiState.value.paymentFlow?.paymentAttempt?.id ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSimulatingPayment = true, actionMessage = null, errorMessage = null)
            }
            when (val result = nikScopedDemoRepository.simulatePaymentSuccess(paymentId)) {
                AppResult.Empty -> showNoBillData(_uiState.value.taxYear ?: 0, "Payment tidak dapat diproses.")
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSimulatingPayment = false,
                            errorMessage = result.message,
                        )
                    }
                }
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSimulatingPayment = false,
                            detail = result.data.taxBill,
                            summary = null,
                            paymentFlow = result.data,
                            errorMessage = null,
                            emptyMessage = null,
                            actionMessage = "Pembayaran demo berhasil. SSPD sudah diterbitkan.",
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadPaymentFlow(nop: Nop, taxYear: Int) {
        when (val result = nikScopedDemoRepository.getOrCreatePaymentFlow(nop, taxYear)) {
            AppResult.Empty -> showNoBillData(taxYear, null)
            is AppResult.Error -> {
                showNoBillData(taxYear, result.message)
            }
            AppResult.Loading -> Unit
            is AppResult.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = result.data.taxBill,
                        summary = null,
                        paymentFlow = result.data,
                        errorMessage = null,
                        emptyMessage = null,
                    )
                }
            }
        }
    }

    private fun showNoBillData(taxYear: Int, fallbackError: String?) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSimulatingPayment = false,
                detail = null,
                summary = null,
                paymentFlow = null,
                errorMessage = null,
                emptyMessage = fallbackError
                    ?: "Data tagihan tahun $taxYear tidak tersedia. Instruksi pembayaran umum tetap ditampilkan.",
            )
        }
    }
}

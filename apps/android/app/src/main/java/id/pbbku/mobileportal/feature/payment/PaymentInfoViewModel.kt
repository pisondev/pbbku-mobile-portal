package id.pbbku.mobileportal.feature.payment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.mapper.toTaxBillDetailOrNull
import id.pbbku.mobileportal.data.mapper.toTaxBillSummaries
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentInfoViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val simpbbRepository = (application as PbbKuApplication).simpbbRepository
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
                detail = null,
                summary = null,
                errorMessage = null,
                emptyMessage = null,
            )
        }
        viewModelScope.launch {
            loadDetail(nop, taxYear)
        }
    }

    fun retry() {
        val state = _uiState.value
        val nop = state.nop ?: return
        val taxYear = state.taxYear ?: return
        load(nop.asDisplayText(), taxYear.toString())
    }

    private suspend fun loadDetail(nop: Nop, taxYear: Int) {
        when (val result = simpbbRepository.getSppt(nop, taxYear)) {
            AppResult.Empty -> loadSummaryFallback(nop, taxYear)
            is AppResult.Error -> {
                loadSummaryFallback(nop, taxYear, fallbackError = result.message)
            }
            AppResult.Loading -> Unit
            is AppResult.Success -> {
                val detail = result.data.json.toTaxBillDetailOrNull(nop, taxYear)
                if (detail == null) {
                    loadSummaryFallback(nop, taxYear)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = detail,
                            summary = null,
                            errorMessage = null,
                            emptyMessage = null,
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadSummaryFallback(
        nop: Nop,
        taxYear: Int,
        fallbackError: String? = null,
    ) {
        when (val result = simpbbRepository.listSpptByNop(nop)) {
            AppResult.Empty -> showNoBillData(taxYear, fallbackError)
            is AppResult.Error -> showNoBillData(taxYear, fallbackError ?: result.message)
            AppResult.Loading -> Unit
            is AppResult.Success -> {
                val summary = result.data.json
                    .toTaxBillSummaries(nop)
                    .firstOrNull { it.taxYear == taxYear }
                if (summary == null) {
                    showNoBillData(taxYear, fallbackError)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = null,
                            summary = summary,
                            errorMessage = null,
                            emptyMessage = null,
                        )
                    }
                }
            }
        }
    }

    private fun showNoBillData(taxYear: Int, fallbackError: String?) {
        _uiState.update {
            it.copy(
                isLoading = false,
                detail = null,
                summary = null,
                errorMessage = null,
                emptyMessage = fallbackError
                    ?: "Data tagihan tahun $taxYear tidak tersedia. Instruksi pembayaran umum tetap ditampilkan.",
            )
        }
    }
}

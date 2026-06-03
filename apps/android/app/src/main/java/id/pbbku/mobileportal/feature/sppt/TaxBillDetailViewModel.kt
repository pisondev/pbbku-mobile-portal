package id.pbbku.mobileportal.feature.sppt

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

class TaxBillDetailViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val nikScopedDemoRepository = (application as PbbKuApplication).nikScopedDemoRepository
    private val _uiState = MutableStateFlow(TaxBillDetailUiState())

    val uiState: StateFlow<TaxBillDetailUiState> = _uiState.asStateFlow()

    fun load(nopDisplay: String, taxYearText: String) {
        val nop = Nop.parseOrNull(nopDisplay)
        val taxYear = taxYearText.toIntOrNull()
        if (nop == null) {
            _uiState.value = TaxBillDetailUiState(errorMessage = "Format NOP tidak valid.")
            return
        }
        if (taxYear == null) {
            _uiState.value = TaxBillDetailUiState(
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
                errorMessage = null,
                emptyMessage = null,
            )
        }
        viewModelScope.launch {
            when (val result = nikScopedDemoRepository.getTaxBill(nop, taxYear)) {
                AppResult.Empty -> showEmpty("Detail tagihan tahun $taxYear tidak tersedia.")
                is AppResult.Error -> showError(result.message)
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = result.data,
                            errorMessage = null,
                            emptyMessage = null,
                        )
                    }
                }
            }
        }
    }

    fun retry() {
        val state = _uiState.value
        val nop = state.nop ?: return
        val taxYear = state.taxYear ?: return
        load(nop.asDisplayText(), taxYear.toString())
    }

    private fun showEmpty(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                detail = null,
                errorMessage = null,
                emptyMessage = message,
            )
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message,
                emptyMessage = null,
            )
        }
    }
}

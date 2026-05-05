package id.pbbku.mobileportal.feature.sppt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.mapper.toTaxBillSummaries
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpptHistoryViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val simpbbRepository = (application as PbbKuApplication).simpbbRepository
    private val _uiState = MutableStateFlow(TaxBillListUiState())

    val uiState: StateFlow<TaxBillListUiState> = _uiState.asStateFlow()

    fun load(nopDisplay: String) {
        val nop = Nop.parseOrNull(nopDisplay)
        if (nop == null) {
            _uiState.value = TaxBillListUiState(errorMessage = "Format NOP tidak valid.")
            return
        }
        _uiState.update {
            it.copy(
                nop = nop,
                isLoading = true,
                errorMessage = null,
                emptyMessage = null,
            )
        }
        viewModelScope.launch {
            val primary = simpbbRepository.listSpptByNop(nop)
            val bills = when (primary) {
                AppResult.Empty -> emptyList()
                is AppResult.Error -> {
                    showError(primary.message)
                    return@launch
                }
                AppResult.Loading -> emptyList()
                is AppResult.Success -> primary.data.json.toTaxBillSummaries(nop)
            }
            val finalBills = if (bills.isNotEmpty()) {
                bills
            } else {
                when (val fallback = simpbbRepository.getSpptHistoryByNop(nop)) {
                    AppResult.Empty -> emptyList()
                    is AppResult.Error -> emptyList()
                    AppResult.Loading -> emptyList()
                    is AppResult.Success -> fallback.data.json.toTaxBillSummaries(nop)
                }
            }
            showBills(finalBills, "Histori SPPT tidak tersedia untuk NOP ini.")
        }
    }

    fun retry() {
        val nop = _uiState.value.nop ?: return
        load(nop.asDisplayText())
    }

    private fun showBills(bills: List<id.pbbku.mobileportal.domain.model.TaxBillSummary>, emptyMessage: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                bills = bills,
                totalActiveAmount = null,
                errorMessage = null,
                emptyMessage = if (bills.isEmpty()) emptyMessage else null,
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

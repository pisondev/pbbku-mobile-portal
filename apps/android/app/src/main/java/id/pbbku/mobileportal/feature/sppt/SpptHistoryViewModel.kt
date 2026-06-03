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

class SpptHistoryViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val pbbKuApplication = application as PbbKuApplication
    private val nikScopedDemoRepository = pbbKuApplication.nikScopedDemoRepository
    private val reminderRepository = pbbKuApplication.paymentReminderRepository
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
            when (val result = nikScopedDemoRepository.listTaxBills(nop)) {
                AppResult.Empty -> showBills(emptyList(), "Histori SPPT tidak tersedia untuk NOP ini.")
                is AppResult.Error -> {
                    showError(result.message)
                    return@launch
                }
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    showBills(result.data, "Histori SPPT tidak tersedia untuk NOP ini.")
                    reminderRepository.scheduleForBills(result.data)
                }
            }
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

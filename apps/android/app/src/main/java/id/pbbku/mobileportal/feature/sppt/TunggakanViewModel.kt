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

class TunggakanViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val pbbKuApplication = application as PbbKuApplication
    private val simpbbRepository = pbbKuApplication.simpbbRepository
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
            when (val result = simpbbRepository.getTunggakanByNop(nop)) {
                AppResult.Empty -> showBills(emptyList())
                is AppResult.Error -> showError(result.message)
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    val bills = result.data.json.toTaxBillSummaries(nop)
                    showBills(bills)
                    reminderRepository.scheduleForBills(bills)
                }
            }
        }
    }

    fun retry() {
        val nop = _uiState.value.nop ?: return
        load(nop.asDisplayText())
    }

    private fun showBills(bills: List<id.pbbku.mobileportal.domain.model.TaxBillSummary>) {
        val total = bills.mapNotNull { it.amount }.takeIf { it.isNotEmpty() }?.sum()
        _uiState.update {
            it.copy(
                isLoading = false,
                bills = bills,
                totalActiveAmount = total,
                errorMessage = null,
                emptyMessage = if (bills.isEmpty()) "Tidak ada tunggakan aktif untuk NOP ini." else null,
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

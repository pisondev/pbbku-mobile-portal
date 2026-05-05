package id.pbbku.mobileportal.feature.building

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.mapper.toBuildingSummaries
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BuildingListViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val simpbbRepository = (application as PbbKuApplication).simpbbRepository
    private val _uiState = MutableStateFlow(BuildingListUiState())

    val uiState: StateFlow<BuildingListUiState> = _uiState.asStateFlow()

    fun load(nopDisplay: String) {
        val nop = Nop.parseOrNull(nopDisplay)
        if (nop == null) {
            _uiState.value = BuildingListUiState(errorMessage = "Format NOP tidak valid.")
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
            when (val result = simpbbRepository.listBangunanByNop(nop)) {
                AppResult.Empty -> showEmpty("Data bangunan tidak tersedia untuk NOP ini.")
                is AppResult.Error -> showError(result.message)
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    val buildings = result.data.json.toBuildingSummaries(nop)
                    if (buildings.isEmpty()) {
                        showEmpty("Data bangunan tidak tersedia untuk NOP ini.")
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                buildings = buildings,
                                errorMessage = null,
                                emptyMessage = null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun retry() {
        val nop = _uiState.value.nop ?: return
        load(nop.asDisplayText())
    }

    private fun showEmpty(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                buildings = emptyList(),
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

package id.pbbku.mobileportal.feature.objectdetail

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

class ObjectDetailViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val app = application as PbbKuApplication
    private val nikScopedDemoRepository = app.nikScopedDemoRepository
    private val _uiState = MutableStateFlow(ObjectDetailUiState())

    val uiState: StateFlow<ObjectDetailUiState> = _uiState.asStateFlow()

    fun load(nopDisplay: String) {
        val nop = Nop.parseOrNull(nopDisplay)
        if (nop == null) {
            _uiState.value = ObjectDetailUiState(
                errorMessage = "Format NOP tidak valid.",
            )
            return
        }
        _uiState.update {
            it.copy(
                nop = nop,
                isLoading = true,
                errorMessage = null,
                emptyMessage = null,
                isCacheData = false,
                cacheTimestampText = null,
            )
        }
        viewModelScope.launch {
            when (val result = nikScopedDemoRepository.getObjekPajakDetail(nop)) {
                AppResult.Empty -> showEmpty("Detail objek pajak tidak ditemukan.")
                is AppResult.Error -> showError(result.message)
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = result.data,
                            errorMessage = null,
                            emptyMessage = null,
                            cacheTimestampText = null,
                            isCacheData = false,
                        )
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
                detail = null,
                errorMessage = null,
                emptyMessage = message,
                cacheTimestampText = null,
                isCacheData = false,
            )
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                detail = null,
                errorMessage = message,
                emptyMessage = null,
                cacheTimestampText = null,
                isCacheData = false,
            )
        }
    }
}

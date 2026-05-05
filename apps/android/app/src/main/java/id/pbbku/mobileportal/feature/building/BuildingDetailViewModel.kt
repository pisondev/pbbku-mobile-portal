package id.pbbku.mobileportal.feature.building

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.mapper.toBuildingDetailOrNull
import id.pbbku.mobileportal.data.mapper.toBuildingFacilities
import id.pbbku.mobileportal.domain.model.BuildingFacility
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BuildingDetailViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val simpbbRepository = (application as PbbKuApplication).simpbbRepository
    private val _uiState = MutableStateFlow(BuildingDetailUiState())

    val uiState: StateFlow<BuildingDetailUiState> = _uiState.asStateFlow()

    fun load(nopDisplay: String, noBng: String) {
        val nop = Nop.parseOrNull(nopDisplay)
        if (nop == null) {
            _uiState.value = BuildingDetailUiState(errorMessage = "Format NOP tidak valid.")
            return
        }
        if (noBng.toIntOrNull() == null) {
            _uiState.value = BuildingDetailUiState(
                nop = nop,
                noBng = noBng,
                errorMessage = "Nomor bangunan tidak valid.",
            )
            return
        }
        _uiState.update {
            it.copy(
                nop = nop,
                noBng = noBng,
                isLoading = true,
                errorMessage = null,
                emptyMessage = null,
                facilitiesMessage = null,
            )
        }
        viewModelScope.launch {
            val detailDeferred = async { simpbbRepository.getBuilding(nop, noBng) }
            val facilitiesDeferred = async { simpbbRepository.listFasilitas(nop, noBng) }
            val detailResult = detailDeferred.await()
            val facilitiesResult = facilitiesDeferred.await()

            val detail = when (detailResult) {
                AppResult.Empty -> null
                is AppResult.Error -> {
                    showError(detailResult.message)
                    return@launch
                }
                AppResult.Loading -> null
                is AppResult.Success -> detailResult.data.json.toBuildingDetailOrNull(nop, noBng)
            }

            if (detail == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = null,
                        facilities = emptyList(),
                        errorMessage = null,
                        emptyMessage = "Detail bangunan tidak tersedia.",
                        facilitiesMessage = null,
                    )
                }
                return@launch
            }

            val facilitiesState = mapFacilities(facilitiesResult)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    detail = detail,
                    facilities = facilitiesState.facilities,
                    errorMessage = null,
                    emptyMessage = null,
                    facilitiesMessage = facilitiesState.message,
                )
            }
        }
    }

    fun retry() {
        val state = _uiState.value
        val nop = state.nop ?: return
        load(nop.asDisplayText(), state.noBng)
    }

    private fun mapFacilities(result: AppResult<id.pbbku.mobileportal.domain.model.ApiPayload>): FacilitiesState {
        return when (result) {
            AppResult.Empty -> FacilitiesState(emptyList(), "Fasilitas bangunan tidak tersedia.")
            is AppResult.Error -> FacilitiesState(emptyList(), result.message)
            AppResult.Loading -> FacilitiesState(emptyList(), null)
            is AppResult.Success -> {
                val facilities = result.data.json.toBuildingFacilities()
                FacilitiesState(
                    facilities = facilities,
                    message = if (facilities.isEmpty()) "Fasilitas bangunan tidak tersedia." else null,
                )
            }
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

    private data class FacilitiesState(
        val facilities: List<BuildingFacility>,
        val message: String?,
    )
}

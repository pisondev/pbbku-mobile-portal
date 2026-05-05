package id.pbbku.mobileportal.feature.objectdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.format.toIndonesianDateTimeText
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.api.SimpbbApiClient
import id.pbbku.mobileportal.data.mapper.toObjekPajakDetailOrNull
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

class ObjectDetailViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val app = application as PbbKuApplication
    private val simpbbRepository = app.simpbbRepository
    private val cacheRepository = app.localCacheRepository
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
            when (val result = simpbbRepository.getObjekPajakByNop(nop)) {
                AppResult.Empty -> loadCacheOrShowEmpty(nop, "Detail objek pajak tidak ditemukan.")
                is AppResult.Error -> loadCacheOrShowError(nop, result.message)
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    val detail = result.data.json.toObjekPajakDetailOrNull()
                    if (detail == null) {
                        loadCacheOrShowEmpty(nop, "Detail objek pajak tidak ditemukan.")
                    } else {
                        cacheRepository.saveReadOnlyCache(
                            cacheKey = cacheKey(nop),
                            payloadJson = result.data.json.toString(),
                            sourceLabel = "SIMPBB OP API",
                        )
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                detail = detail,
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
    }

    fun retry() {
        val nop = _uiState.value.nop ?: return
        load(nop.asDisplayText())
    }

    private suspend fun loadCacheOrShowEmpty(nop: Nop, message: String) {
        loadCache(nop, fallbackMessage = message, asError = false)
    }

    private suspend fun loadCacheOrShowError(nop: Nop, message: String) {
        loadCache(nop, fallbackMessage = message, asError = true)
    }

    private suspend fun loadCache(
        nop: Nop,
        fallbackMessage: String,
        asError: Boolean,
    ) {
        val cache = cacheRepository.getByKey(cacheKey(nop))
        val detail = cache?.payloadJson
            ?.let { runCatching { SimpbbApiClient.json.decodeFromString<JsonElement>(it) }.getOrNull() }
            ?.toObjekPajakDetailOrNull()
        val cachedAt = cache?.updatedAtEpochMillis

        _uiState.update {
            if (detail != null && cachedAt != null) {
                it.copy(
                    isLoading = false,
                    detail = detail,
                    errorMessage = null,
                    emptyMessage = null,
                    cacheTimestampText = cachedAt.toIndonesianDateTimeText(),
                    isCacheData = true,
                )
            } else {
                it.copy(
                    isLoading = false,
                    detail = null,
                    errorMessage = if (asError) fallbackMessage else null,
                    emptyMessage = if (asError) null else fallbackMessage,
                    cacheTimestampText = null,
                    isCacheData = false,
                )
            }
        }
    }

    private fun cacheKey(nop: Nop): String = "object_detail:${nop.asDisplayText()}"
}

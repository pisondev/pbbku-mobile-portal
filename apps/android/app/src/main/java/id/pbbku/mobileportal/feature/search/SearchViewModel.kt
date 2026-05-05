package id.pbbku.mobileportal.feature.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.mapper.toObjekPajakPage
import id.pbbku.mobileportal.data.mapper.toObjekPajakSearchRows
import id.pbbku.mobileportal.domain.model.ObjekPajakSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val simpbbRepository = (application as PbbKuApplication).simpbbRepository
    private val _uiState = MutableStateFlow(SearchUiState())
    private var searchJob: Job? = null
    private var currentListOffset = 0

    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChanged(query: String) {
        val normalized = query.take(64)
        _uiState.update {
            it.copy(
                query = normalized,
                errorMessage = null,
                emptyMessage = if (normalized.length < MIN_QUERY_LENGTH) {
                    "Ketik minimal $MIN_QUERY_LENGTH karakter untuk mencari."
                } else {
                    null
                },
                modeLabel = "Search",
                totalRows = null,
                canLoadMore = false,
            )
        }
        searchJob?.cancel()
        if (normalized.length < MIN_QUERY_LENGTH) {
            _uiState.update { it.copy(isLoading = false, results = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            search()
        }
    }

    fun retry() {
        if (_uiState.value.modeLabel == "Daftar demo") {
            loadDemoList(reset = true)
        } else {
            search()
        }
    }

    fun loadDemoList(reset: Boolean) {
        searchJob?.cancel()
        viewModelScope.launch {
            val query = _uiState.value.query.takeIf { it.isNotBlank() } ?: DEFAULT_DEMO_SEARCH
            val offset = if (reset) 0 else currentListOffset
            if (reset) currentListOffset = 0
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    emptyMessage = null,
                    modeLabel = "Daftar demo",
                    query = query,
                )
            }

            when (
                val result = simpbbRepository.listObjekPajakDetails(
                    kdPropinsi = DEFAULT_KD_PROPINSI,
                    kdDati2 = DEFAULT_KD_DATI2,
                    limit = PAGE_SIZE,
                    offset = offset,
                    search = query,
                )
            ) {
                AppResult.Empty -> showEmpty("Data objek pajak tidak ditemukan.")
                is AppResult.Error -> showError(result.message)
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    val page = result.data.json.toObjekPajakPage()
                    val merged = if (reset) page.rows else _uiState.value.results + page.rows
                    currentListOffset = merged.size
                    showRows(
                        rows = merged,
                        emptyMessage = "Data objek pajak tidak ditemukan.",
                        modeLabel = "Daftar demo",
                        totalRows = page.total,
                        canLoadMore = page.total?.let { merged.size < it } ?: page.rows.size == PAGE_SIZE,
                    )
                }
            }
        }
    }

    private fun search() {
        viewModelScope.launch {
            val query = _uiState.value.query.trim()
            if (query.length < MIN_QUERY_LENGTH) return@launch
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    emptyMessage = null,
                    modeLabel = "Search",
                    totalRows = null,
                    canLoadMore = false,
                )
            }
            when (val result = simpbbRepository.searchObjekPajak(query = query, limit = PAGE_SIZE)) {
                AppResult.Empty -> showEmpty("Hasil pencarian tidak ditemukan.")
                is AppResult.Error -> showError(result.message)
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    showRows(
                        rows = result.data.json.toObjekPajakSearchRows(),
                        emptyMessage = "Hasil pencarian tidak ditemukan.",
                        modeLabel = "Search",
                        totalRows = null,
                        canLoadMore = false,
                    )
                }
            }
        }
    }

    private fun showRows(
        rows: List<ObjekPajakSummary>,
        emptyMessage: String,
        modeLabel: String,
        totalRows: Int?,
        canLoadMore: Boolean,
    ) {
        _uiState.update {
            it.copy(
                isLoading = false,
                results = rows,
                errorMessage = null,
                emptyMessage = if (rows.isEmpty()) emptyMessage else null,
                modeLabel = modeLabel,
                totalRows = totalRows,
                canLoadMore = canLoadMore,
            )
        }
    }

    private fun showEmpty(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                results = emptyList(),
                errorMessage = null,
                emptyMessage = message,
                totalRows = null,
                canLoadMore = false,
            )
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message,
                emptyMessage = null,
                canLoadMore = false,
            )
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 500L
        private const val MIN_QUERY_LENGTH = 3
        private const val PAGE_SIZE = 10
        private const val DEFAULT_DEMO_SEARCH = "BUDI"
        private const val DEFAULT_KD_PROPINSI = "32"
        private const val DEFAULT_KD_DATI2 = "04"
    }
}

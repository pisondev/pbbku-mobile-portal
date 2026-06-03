package id.pbbku.mobileportal.feature.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pbbku.mobileportal.PbbKuApplication
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.mapper.toObjekPajakPage
import id.pbbku.mobileportal.data.mapper.toObjekPajakSearchRows
import id.pbbku.mobileportal.domain.model.ObjekPajakSummary
import id.pbbku.mobileportal.domain.model.WilayahItem
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
    private val pbbKuApplication = application as PbbKuApplication
    private val simpbbRepository = pbbKuApplication.simpbbRepository
    private val wilayahRepository = pbbKuApplication.wilayahRepository
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
                emptyMessage = if (normalized.length < SearchConfig.MIN_QUERY_LENGTH) {
                    "Ketik minimal ${SearchConfig.MIN_QUERY_LENGTH} karakter untuk mencari."
                } else {
                    null
                },
                modeLabel = "Pencarian",
                totalRows = null,
                canLoadMore = false,
            )
        }
        searchJob?.cancel()
        if (normalized.length < SearchConfig.MIN_QUERY_LENGTH) {
            _uiState.update { it.copy(isLoading = false, results = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SearchConfig.SEARCH_DEBOUNCE_MS)
            search()
        }
    }

    fun retry() {
        if (_uiState.value.modeLabel == "Daftar Objek") {
            loadDemoList(reset = true)
        } else {
            search()
        }
    }

    fun loadPropinsiIfNeeded() {
        if (_uiState.value.wilayahFilter.propinsi.isNotEmpty()) return
        viewModelScope.launch {
            setWilayahLoading()
            applyWilayahResult(
                result = wilayahRepository.listPropinsi(),
                update = { state, rows ->
                    state.copy(propinsi = rows, errorMessage = null, isLoading = false)
                },
            )
        }
    }

    fun selectPropinsi(item: WilayahItem?) {
        _uiState.update {
            it.copy(
                wilayahFilter = it.wilayahFilter.copy(
                    selectedPropinsi = item,
                    selectedDati2 = null,
                    selectedKecamatan = null,
                    selectedKelurahan = null,
                    selectedBlok = null,
                    dati2 = emptyList(),
                    kecamatan = emptyList(),
                    kelurahan = emptyList(),
                    blok = emptyList(),
                    errorMessage = null,
                ),
            )
        }
        if (item != null) loadDati2(item.code)
    }

    fun selectDati2(item: WilayahItem?) {
        val propinsi = _uiState.value.wilayahFilter.selectedPropinsi ?: return
        _uiState.update {
            it.copy(
                wilayahFilter = it.wilayahFilter.copy(
                    selectedDati2 = item,
                    selectedKecamatan = null,
                    selectedKelurahan = null,
                    selectedBlok = null,
                    kecamatan = emptyList(),
                    kelurahan = emptyList(),
                    blok = emptyList(),
                    errorMessage = null,
                ),
            )
        }
        if (item != null) loadKecamatan(propinsi.code, item.code)
    }

    fun selectKecamatan(item: WilayahItem?) {
        val filter = _uiState.value.wilayahFilter
        val propinsi = filter.selectedPropinsi ?: return
        val dati2 = filter.selectedDati2 ?: return
        _uiState.update {
            it.copy(
                wilayahFilter = it.wilayahFilter.copy(
                    selectedKecamatan = item,
                    selectedKelurahan = null,
                    selectedBlok = null,
                    kelurahan = emptyList(),
                    blok = emptyList(),
                    errorMessage = null,
                ),
            )
        }
        if (item != null) loadKelurahan(propinsi.code, dati2.code, item.code)
    }

    fun selectKelurahan(item: WilayahItem?) {
        val filter = _uiState.value.wilayahFilter
        val propinsi = filter.selectedPropinsi ?: return
        val dati2 = filter.selectedDati2 ?: return
        val kecamatan = filter.selectedKecamatan ?: return
        _uiState.update {
            it.copy(
                wilayahFilter = it.wilayahFilter.copy(
                    selectedKelurahan = item,
                    selectedBlok = null,
                    blok = emptyList(),
                    errorMessage = null,
                ),
            )
        }
        if (item != null) loadBlok(propinsi.code, dati2.code, kecamatan.code, item.code)
    }

    fun selectBlok(item: WilayahItem?) {
        _uiState.update {
            it.copy(
                wilayahFilter = it.wilayahFilter.copy(
                    selectedBlok = item,
                    errorMessage = null,
                ),
            )
        }
    }

    fun clearWilayahFilter() {
        _uiState.update {
            it.copy(
                wilayahFilter = WilayahFilterUiState(propinsi = it.wilayahFilter.propinsi),
            )
        }
    }

    fun loadDemoList(reset: Boolean) {
        searchJob?.cancel()
        viewModelScope.launch {
            val query = _uiState.value.query.takeIf { it.isNotBlank() } ?: SearchConfig.DEFAULT_DEMO_SEARCH
            val offset = if (reset) 0 else currentListOffset
            val filter = _uiState.value.wilayahFilter
            val selectedPropinsi = filter.selectedPropinsi?.code
            val selectedDati2 = filter.selectedDati2?.code
            if (reset) currentListOffset = 0
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    emptyMessage = null,
                    modeLabel = "Daftar Objek",
                    query = query,
                )
            }

            when (
                val result = simpbbRepository.listObjekPajakDetails(
                    kdPropinsi = selectedPropinsi ?: SearchConfig.DEFAULT_KD_PROPINSI,
                    kdDati2 = selectedDati2 ?: if (selectedPropinsi == null) SearchConfig.DEFAULT_KD_DATI2 else null,
                    limit = SearchConfig.PAGE_SIZE,
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
                        modeLabel = "Daftar Objek",
                        totalRows = page.total,
                        canLoadMore = page.total?.let { merged.size < it } ?: page.rows.size == SearchConfig.PAGE_SIZE,
                    )
                }
            }
        }
    }

    private fun loadDati2(kdPropinsi: String) {
        viewModelScope.launch {
            setWilayahLoading()
            applyWilayahResult(
                result = wilayahRepository.listDati2(kdPropinsi),
                update = { state, rows ->
                    state.copy(dati2 = rows, errorMessage = null, isLoading = false)
                },
            )
        }
    }

    private fun loadKecamatan(kdPropinsi: String, kdDati2: String) {
        viewModelScope.launch {
            setWilayahLoading()
            applyWilayahResult(
                result = wilayahRepository.listKecamatan(kdPropinsi, kdDati2),
                update = { state, rows ->
                    state.copy(kecamatan = rows, errorMessage = null, isLoading = false)
                },
            )
        }
    }

    private fun loadKelurahan(kdPropinsi: String, kdDati2: String, kdKecamatan: String) {
        viewModelScope.launch {
            setWilayahLoading()
            applyWilayahResult(
                result = wilayahRepository.listKelurahan(kdPropinsi, kdDati2, kdKecamatan),
                update = { state, rows ->
                    state.copy(kelurahan = rows, errorMessage = null, isLoading = false)
                },
            )
        }
    }

    private fun loadBlok(
        kdPropinsi: String,
        kdDati2: String,
        kdKecamatan: String,
        kdKelurahan: String,
    ) {
        viewModelScope.launch {
            setWilayahLoading()
            applyWilayahResult(
                result = wilayahRepository.listBlok(kdPropinsi, kdDati2, kdKecamatan, kdKelurahan),
                update = { state, rows ->
                    state.copy(blok = rows, errorMessage = null, isLoading = false)
                },
            )
        }
    }

    private fun setWilayahLoading() {
        _uiState.update {
            it.copy(
                wilayahFilter = it.wilayahFilter.copy(
                    isLoading = true,
                    errorMessage = null,
                ),
            )
        }
    }

    private fun applyWilayahResult(
        result: AppResult<List<WilayahItem>>,
        update: (WilayahFilterUiState, List<WilayahItem>) -> WilayahFilterUiState,
    ) {
        when (result) {
            AppResult.Empty -> setWilayahError("Data wilayah tidak ditemukan.")
            is AppResult.Error -> setWilayahError(result.message)
            AppResult.Loading -> Unit
            is AppResult.Success -> {
                _uiState.update {
                    it.copy(wilayahFilter = update(it.wilayahFilter, result.data))
                }
            }
        }
    }

    private fun setWilayahError(message: String) {
        _uiState.update {
            it.copy(
                wilayahFilter = it.wilayahFilter.copy(
                    isLoading = false,
                    errorMessage = message,
                ),
            )
        }
    }

    private fun search() {
        viewModelScope.launch {
            val query = _uiState.value.query.trim()
            if (query.length < SearchConfig.MIN_QUERY_LENGTH) return@launch
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    emptyMessage = null,
                    modeLabel = "Pencarian",
                    totalRows = null,
                    canLoadMore = false,
                )
            }
            when (val result = simpbbRepository.searchObjekPajak(query = query, limit = SearchConfig.PAGE_SIZE)) {
                AppResult.Empty -> showEmpty("Hasil pencarian tidak ditemukan.")
                is AppResult.Error -> showError(result.message)
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    showRows(
                        rows = result.data.json.toObjekPajakSearchRows(),
                        emptyMessage = "Hasil pencarian tidak ditemukan.",
                        modeLabel = "Pencarian",
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

    companion object
}

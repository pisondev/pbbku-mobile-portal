package id.pbbku.mobileportal.feature.objectdetail

import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.ObjekPajakDetail

data class ObjectDetailUiState(
    val nop: Nop? = null,
    val isLoading: Boolean = false,
    val detail: ObjekPajakDetail? = null,
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
    val cacheTimestampText: String? = null,
    val isCacheData: Boolean = false,
)

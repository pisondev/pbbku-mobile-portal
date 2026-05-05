package id.pbbku.mobileportal.feature.sppt

import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.TaxBillDetail
import id.pbbku.mobileportal.domain.model.TaxBillSummary

data class TaxBillListUiState(
    val nop: Nop? = null,
    val isLoading: Boolean = false,
    val bills: List<TaxBillSummary> = emptyList(),
    val totalActiveAmount: Double? = null,
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
)

data class TaxBillDetailUiState(
    val nop: Nop? = null,
    val taxYear: Int? = null,
    val isLoading: Boolean = false,
    val detail: TaxBillDetail? = null,
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
)

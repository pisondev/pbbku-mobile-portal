package id.pbbku.mobileportal.feature.payment

import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.PaymentFlowData
import id.pbbku.mobileportal.domain.model.TaxBillDetail
import id.pbbku.mobileportal.domain.model.TaxBillSummary

data class PaymentInfoUiState(
    val nop: Nop? = null,
    val taxYear: Int? = null,
    val isLoading: Boolean = false,
    val isSimulatingPayment: Boolean = false,
    val detail: TaxBillDetail? = null,
    val summary: TaxBillSummary? = null,
    val paymentFlow: PaymentFlowData? = null,
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
    val actionMessage: String? = null,
) {
    val hasBillData: Boolean = detail != null || summary != null
}

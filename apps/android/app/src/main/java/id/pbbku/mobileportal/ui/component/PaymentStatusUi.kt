package id.pbbku.mobileportal.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.pbbku.mobileportal.core.format.toPaymentStatusText
import id.pbbku.mobileportal.domain.model.PaymentStatus

@Composable
fun PaymentStatus.statusColor(): Color {
    return when (this) {
        PaymentStatus.PAID -> Color(0xFF047857)
        PaymentStatus.UNPAID -> MaterialTheme.colorScheme.tertiary
        PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.error
        PaymentStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun PaymentStatusLabel(
    status: PaymentStatus,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = status.statusColor().copy(alpha = 0.12f),
        contentColor = status.statusColor(),
    ) {
        Text(
            text = status.toPaymentStatusText(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

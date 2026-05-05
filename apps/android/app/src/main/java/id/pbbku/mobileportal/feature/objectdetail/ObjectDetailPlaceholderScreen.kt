package id.pbbku.mobileportal.feature.objectdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.pbbku.mobileportal.domain.model.Nop

@Composable
fun ObjectDetailPlaceholderScreen(
    nopDisplay: String,
    onBack: () -> Unit,
) {
    val nop = Nop.parseOrNull(nopDisplay)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Detail Objek Pajak",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = nop?.asGroupedText() ?: nopDisplay,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Halaman detail penuh akan diimplementasikan pada Tahap 6.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
        ) {
            Text("Kembali")
        }
    }
}

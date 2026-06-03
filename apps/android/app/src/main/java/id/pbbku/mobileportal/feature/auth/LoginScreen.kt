package id.pbbku.mobileportal.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import id.pbbku.mobileportal.R
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PageHeader

@Composable
fun LoginScreen(
    onSubmitNik: (String) -> Boolean,
    onOtpRequested: () -> Unit,
) {
    var nik by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, top = 28.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PBB-Ku",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Portal PBB-P2 untuk cek objek, tagihan, tunggakan, dan laporan perubahan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            AppCard(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Image(
                    painter = painterResource(R.drawable.login),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.45f)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
                InfoPill(text = "Login Wajib Pajak")
                PageHeader(
                    title = "Masuk",
                    subtitle = "Gunakan NIK 16 digit. Setelah masuk, NIK disembunyikan dan hanya tampil saat kamu menekannya.",
                )
                OutlinedTextField(
                    value = nik,
                    onValueChange = { value ->
                        nik = value.filter(Char::isDigit).take(16)
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("NIK") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = {
                        Text(error ?: "${nik.length}/16 digit")
                    },
                    isError = error != null,
                )
                Button(
                    onClick = {
                        if (onSubmitNik(nik)) {
                            onOtpRequested()
                        } else {
                            error = "NIK harus 16 digit angka."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Lanjut ke OTP")
                }
            }
        }
    }
}

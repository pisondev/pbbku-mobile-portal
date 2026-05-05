package id.pbbku.mobileportal.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PageHeader

@Composable
fun OtpScreen(
    onVerifyOtp: (String, () -> Unit, () -> Unit) -> Unit,
    onVerified: () -> Unit,
) {
    var otp by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        item {
            AppCard {
                InfoPill(text = "OTP demo: ${AuthViewModel.DEMO_OTP}")
                PageHeader(
                    title = "Verifikasi OTP",
                    subtitle = "Masukkan kode demo untuk melanjutkan ke Beranda.",
                )
                OutlinedTextField(
                    value = otp,
                    onValueChange = { value ->
                        otp = value.filter(Char::isDigit).take(6)
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("OTP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text(error ?: "${otp.length}/6 digit") },
                    isError = error != null,
                )
                Button(
                    onClick = {
                        onVerifyOtp(
                            otp,
                            onVerified,
                            { error = "OTP tidak valid." },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Verifikasi")
                }
            }
        }
    }
}

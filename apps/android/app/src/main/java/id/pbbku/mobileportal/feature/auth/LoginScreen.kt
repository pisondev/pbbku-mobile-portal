package id.pbbku.mobileportal.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onSubmitNik: (String) -> Boolean,
    onOtpRequested: () -> Unit,
) {
    var nik by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Login Simulatif",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Masukkan NIK 16 digit untuk menerima OTP demo.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 18.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text("Kirim OTP")
        }
    }
}

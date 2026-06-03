package id.pbbku.mobileportal.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.pbbku.mobileportal.R
import id.pbbku.mobileportal.ui.component.InfoPill

private data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String,
)

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
) {
    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.onboarding_1,
            title = "Cari objek pajak",
            description = "Temukan objek PBB berdasarkan NOP atau nama wajib pajak, lalu buka detail data yang tersedia.",
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_2,
            title = "Lihat SPPT dan tunggakan",
            description = "Pantau histori SPPT, nominal tagihan, status pembayaran, jatuh tempo, dan tunggakan aktif.",
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_3,
            title = "Aktifkan pengingat lokal",
            description = "Simpan reminder di perangkat untuk membantu mengingat jatuh tempo pembayaran PBB.",
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_4,
            title = "Buat draft laporan",
            description = "Susun draft perubahan data bangunan sebagai prototipe lokal tanpa mengubah data resmi SIMPBB.",
        ),
    )
    var pageIndex by rememberSaveable { mutableStateOf(0) }
    val page = pages[pageIndex]
    val isLastPage = pageIndex == pages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 28.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        InfoPill(text = "Portal wajib pajak PBB-P2")
        Text(
            text = "PBB-Ku",
            modifier = Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Akses layanan PBB-P2 dari satu aplikasi Android.",
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.08f)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop,
        )

        Text(
            text = page.title,
            modifier = Modifier.padding(top = 22.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = page.description,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(20.dp))

        PageIndicator(
            currentPage = pageIndex,
            pageCount = pages.size,
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (isLastPage) {
                    onContinue()
                } else {
                    pageIndex += 1
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLastPage) "Masuk" else "Next")
        }
    }
}

@Composable
private fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Surface(
                modifier = Modifier.size(if (index == currentPage) 10.dp else 8.dp),
                shape = CircleShape,
                color = if (index == currentPage) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                content = {},
            )
        }
    }
}

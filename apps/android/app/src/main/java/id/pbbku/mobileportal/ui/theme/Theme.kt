package id.pbbku.mobileportal.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PbbKuLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    secondary = Color(0xFF334155),
    onSecondary = Color.White,
    tertiary = Color(0xFFB45309),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    error = Color(0xFFB91C1C),
)

@Composable
fun PBBKuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PbbKuLightColors,
        content = content,
    )
}

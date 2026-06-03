package id.pbbku.mobileportal.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val PbbKuLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF0B6B63),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEAFE),
    onPrimaryContainer = Color(0xFF0F2A5F),
    secondary = Color(0xFF334155),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6ECF5),
    onSecondaryContainer = Color(0xFF172033),
    tertiary = Color(0xFFB45309),
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = Color(0xFF4A2600),
    background = Color(0xFFF3F7F8),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE8EEF7),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFB91C1C),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

private val PbbKuTypography = Typography(
    headlineMedium = Typography().headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = Typography().headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Typography().titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = Typography().labelLarge.copy(fontWeight = FontWeight.SemiBold),
)

private val PbbKuShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

@Composable
fun PBBKuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PbbKuLightColors,
        typography = PbbKuTypography,
        shapes = PbbKuShapes,
        content = content,
    )
}

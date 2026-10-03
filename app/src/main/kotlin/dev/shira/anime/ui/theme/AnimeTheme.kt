package dev.shira.anime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val VoidBlack = Color(0xFF131315)
val SurfaceLowest = Color(0xFF0E0E10)
val InkPanel = Color(0xFF201F21)
val ElevatedPanel = Color(0xFF2A2A2C)
val SurfaceHighest = Color(0xFF353437)
val VioletSignal = Color(0xFF00E5FF)
val SakuraPulse = Color(0xFFFF4B89)
val MoonText = Color(0xFFE5E1E4)
val MistText = Color(0xFFBAC9CC)
val LineDark = Color(0xFF3B494C)
val AmberFrame = Color(0xFFF6B44B)

private val AnimeDarkColorScheme = darkColorScheme(
    primary = VioletSignal,
    secondary = SakuraPulse,
    tertiary = AmberFrame,
    background = VoidBlack,
    surface = InkPanel,
    surfaceVariant = SurfaceHighest,
    onPrimary = Color(0xFF001F24),
    onSecondary = Color(0xFF3F0019),
    onBackground = MoonText,
    onSurface = MoonText,
    onSurfaceVariant = MistText
)

private val AnimeLightColorScheme = lightColorScheme(
    primary = VioletSignal,
    secondary = SakuraPulse,
    tertiary = AmberFrame,
    background = VoidBlack,
    surface = InkPanel,
    surfaceVariant = SurfaceHighest,
    onPrimary = Color(0xFF001F24),
    onSecondary = Color(0xFF3F0019),
    onBackground = MoonText,
    onSurface = MoonText,
    onSurfaceVariant = MistText
)

private val AnimeTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.8).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

@Composable
fun AnimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = if (darkTheme) AnimeDarkColorScheme else AnimeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AnimeTypography,
        content = content
    )
}

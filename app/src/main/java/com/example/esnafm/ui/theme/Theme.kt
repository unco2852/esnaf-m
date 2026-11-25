package com.example.esnafm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = DarkInk,
    primaryContainer = DarkInk,
    onPrimaryContainer = WarmCream,
    secondary = Turquoise,
    onSecondary = DarkInk,
    secondaryContainer = DarkInk,
    onSecondaryContainer = LightBeige,
    tertiary = Terracotta,
    onTertiary = WarmWhite,
    background = DarkInk,
    onBackground = WarmCream,
    surface = DarkInk,
    onSurface = WarmCream,
    surfaceVariant = DarkInk,
    onSurfaceVariant = LightBeige,
    outline = LightBeige.copy(alpha = 0.5f),
    outlineVariant = LightBeige.copy(alpha = 0.2f),
    error = Color(0xFFCF6679),
    onError = DarkInk,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = Turquoise,
    onPrimary = WarmWhite,
    primaryContainer = LightBeige,
    onPrimaryContainer = DarkInk,
    secondary = SkyBlue,
    onSecondary = DarkInk,
    secondaryContainer = LightBeige,
    onSecondaryContainer = DarkInk,
    tertiary = Terracotta,
    onTertiary = WarmWhite,
    background = WarmCream,
    onBackground = DarkInk,
    surface = WarmWhite,
    onSurface = DarkInk,
    surfaceVariant = LightBeige,
    onSurfaceVariant = DarkInk,
    outline = DarkInk.copy(alpha = 0.5f),
    outlineVariant = LightBeige,
    error = Color(0xFFD32F2F),
    onError = WarmWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

/**
 * Açık ve koyu temalarda yüksek okunabilirlik ve net kontrast sağlanan OutlinedTextField renk stili
 */
@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DarkInk,
    unfocusedTextColor = DarkInk,
    focusedContainerColor = WarmWhite,
    unfocusedContainerColor = WarmWhite,
    disabledContainerColor = LightBeige,
    errorContainerColor = Color(0xFFFFDAD6),
    focusedBorderColor = Turquoise,
    unfocusedBorderColor = DarkInk.copy(alpha = 0.4f),
    disabledBorderColor = DarkInk.copy(alpha = 0.2f),
    errorBorderColor = Terracotta,
    focusedLabelColor = DarkInk,
    unfocusedLabelColor = DarkInk.copy(alpha = 0.7f),
    disabledLabelColor = DarkInk.copy(alpha = 0.4f),
    errorLabelColor = Terracotta,
    focusedPlaceholderColor = DarkInk.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = DarkInk.copy(alpha = 0.6f),
    focusedLeadingIconColor = Turquoise,
    unfocusedLeadingIconColor = DarkInk.copy(alpha = 0.7f),
    focusedTrailingIconColor = Turquoise,
    unfocusedTrailingIconColor = DarkInk.copy(alpha = 0.7f),
    cursorColor = Turquoise
)

/**
 * Esnafım Mahalle Sıcaklığında Jetpack Compose Teması
 */
@Composable
fun EsnafımTheme(
    // Ekranların renk paleti açık tema için tasarlandı. Sistem koyu modunu
    // otomatik uygulamak, açık zeminlerle koyu mod metin renklerini karıştırıyordu.
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Mahalle esnafı renk paletini her cihazda tutarlı korumak için kapalı
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

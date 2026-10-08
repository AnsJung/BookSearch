package com.example.booksearch.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BookLightColorScheme = lightColorScheme(
    primary = BookAccent,
    onPrimary = BookSurface,
    primaryContainer = BookAccentSubtle,
    onPrimaryContainer = BookAccent,
    background = BookBackground,
    onBackground = BookTextPrimary,
    surface = BookSurface,
    surfaceVariant = BookDisabledContainer,
    onSurface = BookTextPrimary,
    onSurfaceVariant = BookTextSecondary,
    outline = BookBorder,
    error = BookError,
)

@Composable
fun BookSearchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BookLightColorScheme,
        typography = BookTypography,
        content = content,
    )
}

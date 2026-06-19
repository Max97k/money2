package com.example.money2.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Rect

val LocalSpotlightRegistry = compositionLocalOf<(String, Rect) -> Unit> {
    { _, _ -> }
}

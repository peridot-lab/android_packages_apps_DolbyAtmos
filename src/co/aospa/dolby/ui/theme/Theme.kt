/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * Material 3 theme supporting dynamic color and reduced-motion schemes.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DolbyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    reduceMotion: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemReduceMotion = rememberReduceMotionState()
    val isReducedMotion = reduceMotion ?: systemReduceMotion.value

    val configuration = LocalConfiguration.current
    val colorScheme =
        remember(darkTheme, dynamicColor, context, configuration) {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) FallbackDarkColorScheme else FallbackLightColorScheme
            }
        }

    val expressiveMotionScheme = remember { MotionScheme.expressive() }
    val motionScheme =
        if (isReducedMotion) {
            SnapMotionScheme
        } else {
            expressiveMotionScheme
        }

    CompositionLocalProvider(LocalReduceMotion provides isReducedMotion) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = motionScheme,
            typography = DolbyTypography,
            content = content,
        )
    }
}

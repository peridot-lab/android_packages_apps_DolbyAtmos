/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme

/**
 * Fallback color schemes when dynamic color is inactive.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val FallbackLightColorScheme: ColorScheme = expressiveLightColorScheme()

val FallbackDarkColorScheme: ColorScheme = darkColorScheme()

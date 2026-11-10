/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp

/** Tabular numerals font feature to prevent horizontal layout shifts during value updates. */
const val TabularNumerals = "tnum"

/**
 * Typography scale applying heading line-breaks and disabling auto-hyphenation.
 */
val DolbyTypography: Typography = run {
    val base = Typography()
    base.copy(
        headlineLarge =
            base.headlineLarge.copy(
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        headlineMedium =
            base.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.25).sp,
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        headlineSmall =
            base.headlineSmall.copy(
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        titleLarge =
            base.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        titleMedium =
            base.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.1.sp,
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        titleSmall =
            base.titleSmall.copy(
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        labelLarge = base.labelLarge.copy(hyphens = Hyphens.None),
        labelMedium = base.labelMedium.copy(hyphens = Hyphens.None),
        labelSmall = base.labelSmall.copy(hyphens = Hyphens.None),
        headlineLargeEmphasized =
            base.headlineLargeEmphasized.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        headlineMediumEmphasized =
            base.headlineMediumEmphasized.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        headlineSmallEmphasized =
            base.headlineSmallEmphasized.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp,
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        titleLargeEmphasized =
            base.titleLargeEmphasized.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.1).sp,
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        titleMediumEmphasized =
            base.titleMediumEmphasized.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        titleSmallEmphasized =
            base.titleSmallEmphasized.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp,
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None,
            ),
        labelLargeEmphasized =
            base.labelLargeEmphasized.copy(
                fontWeight = FontWeight.Bold,
                hyphens = Hyphens.None,
            ),
        labelMediumEmphasized =
            base.labelMediumEmphasized.copy(
                fontWeight = FontWeight.Bold,
                hyphens = Hyphens.None,
            ),
        labelSmallEmphasized =
            base.labelSmallEmphasized.copy(
                fontWeight = FontWeight.Bold,
                hyphens = Hyphens.None,
            ),
    )
}

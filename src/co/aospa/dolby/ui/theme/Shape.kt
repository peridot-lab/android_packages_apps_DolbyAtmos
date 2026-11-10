/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon

/**
 * Scales a normalized [Morph] (0..1 coordinate space) to the target [size].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Immutable
class MorphShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        val clampedProgress = progress.coerceIn(0f, 1f)
        morph.toPath(progress = clampedProgress, path = path)
        val matrix = Matrix().apply { scale(x = size.width, y = size.height) }
        path.transform(matrix)
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MorphShape) return false
        return morph == other.morph && progress == other.progress
    }

    override fun hashCode(): Int {
        var result = morph.hashCode()
        result = 31 * result + progress.hashCode()
        return result
    }
}

/**
 * Remembers a [Morph] and returns a [MorphShape] updating with [progress].
 * Ensures the underlying [Morph] pair is allocated only once per [start] and [end] combination.
 */
@Composable
fun rememberMorphShape(
    start: RoundedPolygon,
    end: RoundedPolygon,
    progress: Float,
): Shape {
    val morph = remember(start, end) { Morph(start, end) }
    val clampedProgress = progress.coerceIn(0f, 1f)
    return remember(morph, clampedProgress) { MorphShape(morph, clampedProgress) }
}

/** Calculates a concentric inner radius (outerRadius - padding). */
fun nestedInnerRadius(outerRadius: Dp, padding: Dp): Dp {
    return (outerRadius - padding).coerceAtLeast(0.dp)
}

/**
 * Pre-allocated polygon pairings and morphs for Dolby Atmos UI.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object DolbyShapes {
    // Master Atmos Hero
    val AtmosOffPolygon: RoundedPolygon = MaterialShapes.Circle
    val AtmosActivePolygon: RoundedPolygon = MaterialShapes.Sunny
    val AtmosStateMorph: Morph by lazy { Morph(AtmosOffPolygon, AtmosActivePolygon) }

    // Sound Profile Shape Budget
    val ProfileDynamicPolygon: RoundedPolygon = MaterialShapes.Burst
    val ProfileMoviePolygon: RoundedPolygon = MaterialShapes.Cookie4Sided
    val ProfileMusicPolygon: RoundedPolygon = MaterialShapes.Flower
    val ProfileVoicePolygon: RoundedPolygon = MaterialShapes.Oval
    val ProfileCustomPolygon: RoundedPolygon = MaterialShapes.SoftBoom

    // IEQ Mood Badges
    val IeqOffPolygon: RoundedPolygon = MaterialShapes.Circle
    val IeqBalancedPolygon: RoundedPolygon = MaterialShapes.Cookie4Sided
    val IeqWarmPolygon: RoundedPolygon = MaterialShapes.SoftBurst
    val IeqDetailedPolygon: RoundedPolygon = MaterialShapes.Sunny

    // IEQ Touch Morphs (Morphs toward Circle on touch)
    val IeqBalancedMorph: Morph by lazy { Morph(IeqBalancedPolygon, MaterialShapes.Circle) }
    val IeqWarmMorph: Morph by lazy { Morph(IeqWarmPolygon, MaterialShapes.Circle) }
    val IeqDetailedMorph: Morph by lazy { Morph(IeqDetailedPolygon, MaterialShapes.Circle) }

    // Equalizer Screen Shapes & Morphs
    val EqNodeNormalPolygon: RoundedPolygon = MaterialShapes.Circle
    val EqNodeActivePolygon: RoundedPolygon = MaterialShapes.Sunny
    val EqNodeMorph: Morph by lazy { Morph(EqNodeNormalPolygon, EqNodeActivePolygon) }

    val SavePresetPolygon: RoundedPolygon = MaterialShapes.Cookie4Sided
    val ResetGainsPolygon: RoundedPolygon = MaterialShapes.Burst
    val SaveButtonMorph: Morph by lazy { Morph(SavePresetPolygon, MaterialShapes.Circle) }
    val ResetButtonMorph: Morph by lazy { Morph(ResetGainsPolygon, MaterialShapes.Circle) }
}

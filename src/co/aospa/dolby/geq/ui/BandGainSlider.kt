/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.geq.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.aospa.dolby.geq.data.BandGain
import co.aospa.dolby.ui.theme.TabularNumerals
import co.aospa.dolby.ui.theme.expressiveFastSpatialSpec
import co.aospa.dolby.ui.theme.rememberDolbyHaptics
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BandGainSlider(
    bandGain: BandGain,
    onValueChangeFinished: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val currentOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)

    // Backend range is -100..100, representing -10.0 dB to +10.0 dB.
    // The slider spans -10.0f..10.0f with 19 interior steps (20 intervals = exactly 1.0 dB increments).
    val initialDb = (bandGain.gain / 10f).coerceIn(-10f, 10f)
    val sliderState =
        remember(bandGain.band) {
            SliderState(
                value = initialDb,
                steps = 19,
                onValueChangeFinished = null,
                valueRange = -10f..10f,
            )
        }

    val haptics = rememberDolbyHaptics()
    var lastHapticStep by remember(bandGain.band) { mutableIntStateOf(initialDb.roundToInt()) }

    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()

    val labelScale by
        animateFloatAsState(
            targetValue = if (isDragged) 1.25f else 1.0f,
            animationSpec = expressiveFastSpatialSpec(),
            label = "BandLabelScale",
        )
    val thumbWidth by
        animateDpAsState(
            targetValue = if (isDragged) 28.dp else 20.dp,
            animationSpec = expressiveFastSpatialSpec(),
            label = "BandThumbWidth",
        )
    val thumbHeight by
        animateDpAsState(
            targetValue = if (isDragged) 6.dp else 4.dp,
            animationSpec = expressiveFastSpatialSpec(),
            label = "BandThumbHeight",
        )

    sliderState.onValueChangeFinished = {
        val gainInt = (sliderState.value * 10f).roundToInt().coerceIn(-100, 100)
        currentOnValueChangeFinished(gainInt)
    }

    LaunchedEffect(bandGain.gain) {
        val targetDb = (bandGain.gain / 10f).coerceIn(-10f, 10f)
        lastHapticStep = targetDb.roundToInt()
        if (sliderState.value != targetDb) {
            sliderState.value = targetDb
        }
    }

    LaunchedEffect(sliderState.value) {
        val currentStep = sliderState.value.roundToInt()
        if (currentStep != lastHapticStep) {
            if (currentStep == 0) {
                haptics.centerDetent()
            } else {
                haptics.stepTick()
            }
            lastHapticStep = currentStep
        }
    }

    val currentGainDb = sliderState.value
    val isNonZero = kotlin.math.abs(currentGainDb) > 0.05f
    val gainText =
        if (currentGainDb > 0.05f) {
            "+%.1f".format(currentGainDb)
        } else if (currentGainDb < -0.05f) {
            "%.1f".format(currentGainDb)
        } else {
            "%.1f".format(0f)
        }

    val freqText =
        if (bandGain.band >= 1000) {
            "${bandGain.band / 1000}k"
        } else {
            "${bandGain.band}"
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            modifier
                .defaultMinSize(minWidth = 48.dp)
                .padding(vertical = 4.dp),
    ) {
        Text(
            text = gainText,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFeatureSettings = TabularNumerals,
                    fontWeight = if (isNonZero) FontWeight.SemiBold else FontWeight.Normal,
                ),
            color =
                when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    isNonZero -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            modifier =
                Modifier.scale(labelScale)
                    .clearAndSetSemantics {},
        )

        val sliderColors =
            SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

        VerticalSlider(
            state = sliderState,
            reverseDirection = true,
            enabled = enabled,
            colors = sliderColors,
            interactionSource = interactionSource,
            thumb = {
                Box(
                    modifier =
                        Modifier.size(width = thumbWidth, height = thumbHeight)
                            .background(
                                color =
                                    if (enabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                    },
                                shape = RoundedCornerShape(2.dp),
                            )
                )
            },
            track = { state ->
                SliderDefaults.CenteredTrack(
                    sliderState = state,
                    enabled = enabled,
                    colors = sliderColors,
                )
            },
            modifier =
                Modifier
                    .height(160.dp)
                    .defaultMinSize(minWidth = 48.dp)
                    .semantics {
                        contentDescription = "$freqText Hz"
                    },
        )

        Text(
            text = freqText,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFeatureSettings = TabularNumerals,
                ),
            color =
                if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

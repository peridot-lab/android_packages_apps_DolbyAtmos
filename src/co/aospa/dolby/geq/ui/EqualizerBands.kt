/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.geq.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import co.aospa.dolby.R
import co.aospa.dolby.geq.data.BandGain
import co.aospa.dolby.ui.theme.TabularNumerals
import co.aospa.dolby.ui.theme.expressiveFastSpatialSpec
import co.aospa.dolby.ui.theme.rememberDolbyHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EqualizerBands(
    bandGains: List<BandGain>,
    pagerState: PagerState,
    onGainChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val coroutineScope = rememberCoroutineScope()
    val haptics = rememberDolbyHaptics()
    var lastPage by remember { mutableIntStateOf(pagerState.currentPage) }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != lastPage) {
            haptics.selectionClick()
            lastPage = pagerState.currentPage
        }
    }

    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isExpanded = maxWidth >= 600.dp

        if (isExpanded) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Canvas(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(1.dp)
                            .align(Alignment.Center)
                            .padding(horizontal = 8.dp),
                ) {
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    drawLine(
                        color = outlineVariant.copy(alpha = 0.45f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect,
                    )
                }
                Row(
                    modifier =
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    bandGains.forEachIndexed { index, bandGain ->
                        BandGainSlider(
                            bandGain = bandGain,
                            enabled = enabled,
                            onValueChangeFinished = { newGain -> onGainChanged(index, newGain) },
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val isLowSelected = pagerState.currentPage == 0
                val lowEndRadius by
                    animateDpAsState(
                        targetValue = if (isLowSelected) 24.dp else 4.dp,
                        animationSpec = expressiveFastSpatialSpec(),
                        label = "LowTabEndRadius",
                    )
                val lowShape =
                    RoundedCornerShape(
                        topStart = 24.dp,
                        bottomStart = 24.dp,
                        topEnd = lowEndRadius,
                        bottomEnd = lowEndRadius,
                    )

                val isHighSelected = pagerState.currentPage == 1
                val highStartRadius by
                    animateDpAsState(
                        targetValue = if (isHighSelected) 24.dp else 4.dp,
                        animationSpec = expressiveFastSpatialSpec(),
                        label = "HighTabStartRadius",
                    )
                val highShape =
                    RoundedCornerShape(
                        topStart = highStartRadius,
                        bottomStart = highStartRadius,
                        topEnd = 24.dp,
                        bottomEnd = 24.dp,
                    )

                Row(
                    modifier =
                        Modifier.selectableGroup().padding(horizontal = 16.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    ToggleButton(
                        checked = isLowSelected,
                        onCheckedChange = {
                            haptics.selectionClick()
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        },
                        shapes = ToggleButtonDefaults.shapes(shape = lowShape),
                        colors =
                            ToggleButtonDefaults.toggleButtonColors(
                                checkedContainerColor = MaterialTheme.colorScheme.primary,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        modifier =
                            Modifier.defaultMinSize(minWidth = 110.dp, minHeight = 48.dp)
                                .semantics { role = Role.RadioButton },
                    ) {
                        Text(
                            text = "${stringResource(R.string.dolby_low)} (32-500 Hz)",
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFeatureSettings = TabularNumerals,
                                ),
                        )
                    }

                    ToggleButton(
                        checked = isHighSelected,
                        onCheckedChange = {
                            haptics.selectionClick()
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        },
                        shapes = ToggleButtonDefaults.shapes(shape = highShape),
                        colors =
                            ToggleButtonDefaults.toggleButtonColors(
                                checkedContainerColor = MaterialTheme.colorScheme.primary,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        modifier =
                            Modifier.defaultMinSize(minWidth = 110.dp, minHeight = 48.dp)
                                .semantics { role = Role.RadioButton },
                    ) {
                        Text(
                            text = "${stringResource(R.string.dolby_high)} (1k-16k Hz)",
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFeatureSettings = TabularNumerals,
                                ),
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    val startIndex = if (page == 0) 0 else 5
                    val endIndex = if (page == 0) 5 else 10
                    val pageBands =
                        if (bandGains.size >= 10) {
                            bandGains.subList(startIndex, endIndex)
                        } else {
                            bandGains
                        }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Canvas(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(1.dp)
                                    .align(Alignment.Center)
                                    .padding(horizontal = 12.dp),
                        ) {
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            drawLine(
                                color = outlineVariant.copy(alpha = 0.45f),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashEffect,
                            )
                        }
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            pageBands.forEachIndexed { relativeIndex, bandGain ->
                                val actualIndex = startIndex + relativeIndex
                                BandGainSlider(
                                    bandGain = bandGain,
                                    enabled = enabled,
                                    onValueChangeFinished = { newGain ->
                                        onGainChanged(actualIndex, newGain)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

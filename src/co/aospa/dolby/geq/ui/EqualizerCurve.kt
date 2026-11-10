/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.geq.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import co.aospa.dolby.geq.data.BandGain
import co.aospa.dolby.ui.theme.DolbyShapes
import co.aospa.dolby.ui.theme.expressiveDefaultSpatialSpec
import co.aospa.dolby.ui.theme.expressiveFastEffectsSpec
import co.aospa.dolby.ui.theme.expressiveFastSpatialSpec
import co.aospa.dolby.ui.theme.rememberDolbyHaptics
import kotlin.math.abs
import kotlin.math.roundToInt

/** Frequency response curve rendering a Catmull-Rom spline across the 10 bands. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EqualizerCurve(
    bandGains: List<BandGain>,
    activePage: Int,
    onNodeClicked: (bandIndex: Int) -> Unit,
    onGainChanged: (bandIndex: Int, newGain: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val currentOnNodeClicked by rememberUpdatedState(onNodeClicked)
    val currentOnGainChanged by rememberUpdatedState(onGainChanged)
    val haptics = rememberDolbyHaptics()

    var draggedBandIndex by remember { mutableStateOf<Int?>(null) }
    var lastDragGain by remember { mutableIntStateOf(0) }

    val animatedGains = List(10) { index ->
        val targetGain = bandGains.getOrNull(index)?.gain?.toFloat() ?: 0f
        animateFloatAsState(
            targetValue = targetGain,
            animationSpec = if (draggedBandIndex == index) snap() else expressiveDefaultSpatialSpec(),
            label = "CurveNodeGain_$index",
        )
    }

    val isLowActive = activePage == -1 || activePage == 0
    val isHighActive = activePage == -1 || activePage == 1
    val lowActiveProgress by animateFloatAsState(
        targetValue = if (isLowActive) 1f else 0f,
        animationSpec = expressiveFastEffectsSpec(),
        label = "CurveLowActiveProgress",
    )
    val highActiveProgress by animateFloatAsState(
        targetValue = if (isHighActive) 1f else 0f,
        animationSpec = expressiveFastEffectsSpec(),
        label = "CurveHighActiveProgress",
    )
    val dragMorphProgress by
        animateFloatAsState(
            targetValue = if (draggedBandIndex != null) 1f else 0f,
            animationSpec = expressiveFastSpatialSpec(),
            label = "CurveNodeDragMorph",
        )

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val paddingX = 20.dp.toPx()
                        val paddingY = 16.dp.toPx()
                        val availableWidth = size.width - 2 * paddingX
                        val availableHeight = size.height - 2 * paddingY
                        val centerY = size.height / 2f

                        val bandStep = availableWidth / 9f
                        val closestIndex =
                            ((offset.x - paddingX) / bandStep)
                                .roundToInt()
                                .coerceIn(0, 9)

                        val nodeX = paddingX + closestIndex * bandStep
                        if (abs(offset.x - nodeX) < 36.dp.toPx()) {
                            currentOnNodeClicked(closestIndex)
                            val normalized = -((offset.y - centerY) / (availableHeight / 2f))
                            val newGain = (normalized * 100f).roundToInt().coerceIn(-100, 100)
                            if (newGain == 0) {
                                haptics.centerDetent()
                            } else {
                                haptics.stepTick()
                            }
                            currentOnGainChanged(closestIndex, newGain)
                        } else {
                            // Tapping left or right half of the graph background switches pager range
                            val targetIndex = if (offset.x < size.width / 2f) 0 else 5
                            haptics.selectionClick()
                            currentOnNodeClicked(targetIndex)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val paddingX = 20.dp.toPx()
                            val availableWidth = size.width - 2 * paddingX
                            val bandStep = availableWidth / 9f
                            val closestIndex =
                                ((offset.x - paddingX) / bandStep)
                                    .roundToInt()
                                    .coerceIn(0, 9)

                            val nodeX = paddingX + closestIndex * bandStep
                            if (abs(offset.x - nodeX) < 36.dp.toPx()) {
                                draggedBandIndex = closestIndex
                                currentOnNodeClicked(closestIndex)
                                val paddingY = 16.dp.toPx()
                                val availableHeight = size.height - 2 * paddingY
                                val centerY = size.height / 2f
                                val normalized = -((offset.y - centerY) / (availableHeight / 2f))
                                val newGain = (normalized * 100f).roundToInt().coerceIn(-100, 100)
                                lastDragGain = newGain
                                if (newGain == 0) {
                                    haptics.centerDetent()
                                } else {
                                    haptics.stepTick()
                                }
                                currentOnGainChanged(closestIndex, newGain)
                            } else {
                                draggedBandIndex = null
                            }
                        },
                        onDragEnd = { draggedBandIndex = null },
                        onDragCancel = { draggedBandIndex = null },
                        onDrag = { change, _ ->
                            val bandIndex = draggedBandIndex ?: return@detectDragGestures
                            val paddingY = 16.dp.toPx()
                            val availableHeight = size.height - 2 * paddingY
                            val centerY = size.height / 2f
                            val normalized =
                                -((change.position.y - centerY) / (availableHeight / 2f))
                            val newGain = (normalized * 100f).roundToInt().coerceIn(-100, 100)
                            if ((newGain >= 0 && lastDragGain < 0) || (newGain <= 0 && lastDragGain > 0) || (newGain == 0 && lastDragGain != 0)) {
                                haptics.centerDetent()
                            } else if (newGain / 10 != lastDragGain / 10) {
                                haptics.stepTick()
                            }
                            lastDragGain = newGain
                            currentOnGainChanged(bandIndex, newGain)
                            change.consume()
                        },
                    )
                }
    ) {
        val paddingX = 20.dp.toPx()
        val paddingY = 16.dp.toPx()
        val availableWidth = size.width - 2 * paddingX
        val availableHeight = size.height - 2 * paddingY
        val centerY = size.height / 2f

        drawLine(
            color = outlineVariantColor,
            start = Offset(paddingX, centerY),
            end = Offset(size.width - paddingX, centerY),
            strokeWidth = 1.dp.toPx(),
        )

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        drawLine(
            color = outlineVariantColor.copy(alpha = 0.4f),
            start = Offset(paddingX, paddingY),
            end = Offset(size.width - paddingX, paddingY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect,
        )
        drawLine(
            color = outlineVariantColor.copy(alpha = 0.4f),
            start = Offset(paddingX, size.height - paddingY),
            end = Offset(size.width - paddingX, size.height - paddingY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect,
        )

        if (bandGains.size != 10) return@Canvas

        val bandStep = availableWidth / 9f
        val points =
            List(10) { index ->
                val gain = animatedGains[index].value
                val x = paddingX + index * bandStep
                val y = centerY - (gain / 100f) * (availableHeight / 2f)
                Offset(x, y.coerceIn(paddingY, size.height - paddingY))
            }

        // Catmull-Rom cubic spline with clamped horizontal boundary tangents
        val curvePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p0 =
                    if (i > 0) {
                        points[i - 1]
                    } else {
                        Offset(points[i].x - bandStep, points[i].y)
                    }
                val p1 = points[i]
                val p2 = points[i + 1]
                val p3 =
                    if (i + 2 < points.size) {
                        points[i + 2]
                    } else {
                        Offset(points[i + 1].x + bandStep, points[i + 1].y)
                    }

                val c1x = p1.x + (p2.x - p0.x) / 6f
                val c1y = (p1.y + (p2.y - p0.y) / 6f).coerceIn(paddingY, size.height - paddingY)
                val c2x = p2.x - (p3.x - p1.x) / 6f
                val c2y = (p2.y - (p3.y - p1.y) / 6f).coerceIn(paddingY, size.height - paddingY)

                cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
            }
        }

        val fillPath = Path().apply {
            addPath(curvePath)
            lineTo(points.last().x, size.height - paddingY)
            lineTo(points.first().x, size.height - paddingY)
            close()
        }

        drawPath(
            path = fillPath,
            brush =
                Brush.verticalGradient(
                    colors =
                        listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                    startY = paddingY,
                    endY = size.height - paddingY,
                ),
        )

        drawPath(
            path = curvePath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )

        for (i in points.indices) {
            val point = points[i]
            val activeProgress = if (i < 5) lowActiveProgress else highActiveProgress
            val isDragged = (draggedBandIndex == i)

            if (isDragged) {
                val nodeSize = 22.dp.toPx()
                val morphPath = Path()
                DolbyShapes.EqNodeMorph.toPath(dragMorphProgress, morphPath)
                val matrix = Matrix().apply { scale(x = nodeSize, y = nodeSize) }
                morphPath.transform(matrix)
                morphPath.translate(Offset(point.x - nodeSize / 2f, point.y - nodeSize / 2f))

                drawPath(path = morphPath, color = primaryColor)
                drawCircle(color = onPrimaryColor, radius = 3.5.dp.toPx(), center = point)
            } else {
                val outerRadius = 5.dp.toPx() + 2.dp.toPx() * activeProgress
                val innerRadius = 2.5.dp.toPx() + 0.5.dp.toPx() * activeProgress
                val outerColor = lerp(outlineVariantColor, primaryColor, activeProgress)
                val innerColor =
                    lerp(onSurfaceVariantColor.copy(alpha = 0.6f), onPrimaryColor, activeProgress)

                drawCircle(
                    color = outerColor,
                    radius = outerRadius,
                    center = point,
                )
                drawCircle(
                    color = innerColor,
                    radius = innerRadius,
                    center = point,
                )
            }
        }
    }
}

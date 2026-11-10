/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.aospa.dolby.R
import co.aospa.dolby.geq.EqualizerActivity
import co.aospa.dolby.ui.theme.DolbyShapes
import co.aospa.dolby.ui.theme.LocalReduceMotion
import co.aospa.dolby.ui.theme.MorphShape
import co.aospa.dolby.ui.theme.TabularNumerals
import co.aospa.dolby.ui.theme.expressiveDefaultEffectsSpec
import co.aospa.dolby.ui.theme.expressiveDefaultSpatialSpec
import co.aospa.dolby.ui.theme.expressiveFastSpatialSpec
import co.aospa.dolby.ui.theme.rememberDolbyHaptics
import kotlin.math.roundToInt

private val ProfileIds = intArrayOf(0, 1, 2, 8, 3)
private val ProfileLabels =
    listOf(
        R.string.dolby_profile_dynamic,
        R.string.dolby_profile_video,
        R.string.dolby_profile_music,
        R.string.dolby_profile_voice,
        R.string.dolby_profile_custom,
    )
private val ProfilePolygons =
    listOf(
        DolbyShapes.ProfileDynamicPolygon,
        DolbyShapes.ProfileMoviePolygon,
        DolbyShapes.ProfileMusicPolygon,
        DolbyShapes.ProfileVoicePolygon,
        DolbyShapes.ProfileCustomPolygon,
    )

// DAX preset IDs defined in dax-default.xml:
// id="2" name="balanced", id="3" name="warm", id="1" name="detailed", id="0" off.
private val IeqValues = intArrayOf(0, 2, 3, 1)
private val IeqLabels =
    listOf(
        R.string.dolby_off,
        R.string.dolby_balanced,
        R.string.dolby_warm,
        R.string.dolby_detailed,
    )
private val IeqIcons =
    listOf(
        R.drawable.ic_ieq_off,
        R.drawable.ic_ieq_balanced,
        R.drawable.ic_ieq_warm,
        R.drawable.ic_ieq_detailed,
    )
private val IeqPolygons =
    listOf(
        DolbyShapes.IeqOffPolygon,
        DolbyShapes.IeqBalancedPolygon,
        DolbyShapes.IeqWarmPolygon,
        DolbyShapes.IeqDetailedPolygon,
    )
private val IeqMorphs =
    listOf(
        null,
        DolbyShapes.IeqBalancedMorph,
        DolbyShapes.IeqWarmMorph,
        DolbyShapes.IeqDetailedMorph,
    )

private val DialogueValues = intArrayOf(0, 2, 6, 9, 12)
private val DialogueLabels =
    listOf(
        R.string.dolby_off,
        R.string.dolby_low,
        R.string.dolby_medium,
        R.string.dolby_high,
        R.string.dolby_max,
    )

private val StereoValues = intArrayOf(4, 24, 44, 64)
private val StereoLabels =
    listOf(
        R.string.dolby_low,
        R.string.dolby_medium,
        R.string.dolby_high,
        R.string.dolby_max,
    )

private fun dialogueValueToIndex(value: Int): Int {
    if (value <= 0) return 0
    var closestIdx = 1
    var minDiff = Int.MAX_VALUE
    for (i in 1..DialogueValues.lastIndex) {
        val diff = kotlin.math.abs(DialogueValues[i] - value)
        if (diff < minDiff) {
            minDiff = diff
            closestIdx = i
        }
    }
    return closestIdx
}

private fun stereoValueToIndex(value: Int): Int {
    var closestIdx = 0
    var minDiff = Int.MAX_VALUE
    for (i in StereoValues.indices) {
        val diff = kotlin.math.abs(StereoValues[i] - value)
        if (diff < minDiff) {
            minDiff = diff
            closestIdx = i
        }
    }
    return closestIdx
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: DolbyViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refresh()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(text = stringResource(R.string.dolby_title))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (backDispatcher != null) {
                                backDispatcher.onBackPressed()
                            } else {
                                (context as? Activity)?.finish()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.dolby_back),
                        )
                    }
                },
                actions = {
                    if (uiState.isProfileActive) {
                        TooltipBox(
                            positionProvider =
                                TooltipDefaults.rememberTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(stringResource(R.string.dolby_reset_profile))
                                }
                            },
                            state = rememberTooltipState(),
                        ) {
                            IconButton(onClick = { showResetDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription =
                                        stringResource(R.string.dolby_reset_profile),
                                )
                            }
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val insets = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
        val insetsPadding = insets.asPaddingValues()
        val bottomInset = insetsPadding.calculateBottomPadding()
        val startCutout = insetsPadding.calculateStartPadding(LocalLayoutDirection.current)
        val endCutout = insetsPadding.calculateEndPadding(LocalLayoutDirection.current)

        BoxWithConstraints(
            modifier =
                Modifier.fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.surface),
        ) {
            val contentModifier =
                if (maxWidth >= 600.dp) {
                    Modifier.widthIn(max = 720.dp).align(Alignment.TopCenter)
                } else {
                    Modifier.fillMaxWidth()
                }

            Column(
                modifier =
                    contentModifier
                        .verticalScroll(rememberScrollState())
                        .padding(start = startCutout, end = endCutout)
                        .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HeroCard(
                    uiState = uiState,
                    onToggleEnabled = { viewModel.setDolbyEnabled(it) },
                )

                ProfileSection(
                    uiState = uiState,
                    onSelectProfile = { viewModel.setProfile(it) },
                )

                IeqSection(
                    uiState = uiState,
                    onSelectIeq = { viewModel.setIeqPreset(it) },
                )

                EqualizerEntryCard(
                    uiState = uiState,
                    context = context,
                )

                EnhancementsSection(
                    uiState = uiState,
                    viewModel = viewModel,
                )

                Spacer(Modifier.height(16.dp + bottomInset))
            }
        }
    }

    if (showResetDialog) {
        val haptics = rememberDolbyHaptics()
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.dolby_reset_profile_confirm_title)) },
            text = { Text(stringResource(R.string.dolby_reset_profile_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptics.confirm()
                        viewModel.resetProfileSettings()
                        showResetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.dolby_reset_profile_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.dolby_cancel))
                }
            },
        )
    }
}

/**
 * Oscillating equalizer bars for the active hero badge; rests when disabled or reduce-motion is set.
 */
@Composable
private fun EqualizerWaveBars(
    isAnimating: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = LocalReduceMotion.current
    val shouldAnimate = isAnimating && !reduceMotion

    val infiniteTransition = rememberInfiniteTransition(label = "EqWaveTransition")
    val phase0 by
        if (shouldAnimate) {
            infiniteTransition.animateFloat(
                initialValue = 0.35f,
                targetValue = 0.95f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "WavePhase0",
            )
        } else {
            remember { mutableFloatStateOf(0.40f) }
        }
    val phase1 by
        if (shouldAnimate) {
            infiniteTransition.animateFloat(
                initialValue = 0.50f,
                targetValue = 1.0f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(520, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "WavePhase1",
            )
        } else {
            remember { mutableFloatStateOf(0.75f) }
        }
    val phase2 by
        if (shouldAnimate) {
            infiniteTransition.animateFloat(
                initialValue = 0.30f,
                targetValue = 0.85f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(780, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "WavePhase2",
            )
        } else {
            remember { mutableFloatStateOf(0.55f) }
        }
    val phase3 by
        if (shouldAnimate) {
            infiniteTransition.animateFloat(
                initialValue = 0.25f,
                targetValue = 0.70f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "WavePhase3",
            )
        } else {
            remember { mutableFloatStateOf(0.35f) }
        }

    Canvas(modifier = modifier) {
        val barWidth = size.width / 9f
        val spacing = size.width / 9f
        val totalWidth = 4 * barWidth + 3 * spacing
        val startX = (size.width - totalWidth) / 2f
        val maxHeight = size.height * 0.75f
        val centerY = size.height / 2f
        val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

        val phases = floatArrayOf(phase0, phase1, phase2, phase3)
        for (i in 0..3) {
            val barHeight = (maxHeight * phases[i]).coerceAtLeast(barWidth)
            val left = startX + i * (barWidth + spacing)
            val top = centerY - barHeight / 2f
            drawRoundRect(
                color = tint,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius,
            )
        }
    }
}

/** Hero card with Atmos status, animated shape badge, resonance ring, and master toggle. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HeroCard(
    uiState: DolbyUiState,
    onToggleEnabled: (Boolean) -> Unit,
) {
    val haptics = rememberDolbyHaptics()
    val reduceMotion = LocalReduceMotion.current
    val shouldAnimatePulse = uiState.dsOn && !reduceMotion
    val containerShape = RoundedCornerShape(32.dp)

    val targetContainerColor =
        if (uiState.dsOn) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    val containerColor by
        animateColorAsState(
            targetValue = targetContainerColor,
            animationSpec = expressiveDefaultEffectsSpec(),
            label = "HeroContainerColor",
        )
    val targetTitleColor =
        if (uiState.dsOn) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    val titleColor by
        animateColorAsState(
            targetValue = targetTitleColor,
            animationSpec = expressiveDefaultEffectsSpec(),
            label = "HeroTitleColor",
        )
    val targetIconBackground =
        if (uiState.dsOn) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }
    val iconBackground by
        animateColorAsState(
            targetValue = targetIconBackground,
            animationSpec = expressiveDefaultEffectsSpec(),
            label = "HeroIconBackground",
        )
    val targetIconTint =
        if (uiState.dsOn) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    val iconTint by
        animateColorAsState(
            targetValue = targetIconTint,
            animationSpec = expressiveDefaultEffectsSpec(),
            label = "HeroIconTint",
        )

    val infiniteTransition = rememberInfiniteTransition(label = "HeroResonance")
    val resonanceScale by
        if (shouldAnimatePulse) {
            infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.30f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(1800, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "ResonanceScale",
            )
        } else {
            remember { mutableFloatStateOf(1.0f) }
        }
    val resonanceAlpha by
        if (shouldAnimatePulse) {
            infiniteTransition.animateFloat(
                initialValue = 0.30f,
                targetValue = 0.0f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(1800, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "ResonanceAlpha",
            )
        } else {
            remember { mutableFloatStateOf(0.0f) }
        }

    val morphProgress by
        animateFloatAsState(
            targetValue = if (uiState.dsOn) 1f else 0f,
            animationSpec = expressiveDefaultSpatialSpec(),
            label = "AtmosStateMorphProgress",
        )
    val iconShape =
        remember(morphProgress) {
            MorphShape(DolbyShapes.AtmosStateMorph, morphProgress)
        }

    Surface(
        checked = uiState.dsOn,
        onCheckedChange = { newState ->
            if (newState) haptics.confirm() else haptics.reject()
            onToggleEnabled(newState)
        },
        shape = containerShape,
        color = containerColor,
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .semantics {
                    role = Role.Switch
                    liveRegion = LiveRegionMode.Polite
                },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp),
                ) {
                    if (shouldAnimatePulse) {
                        Box(
                            modifier =
                                Modifier.size(72.dp)
                                    .graphicsLayer {
                                        scaleX = resonanceScale
                                        scaleY = resonanceScale
                                        alpha = resonanceAlpha
                                    }
                                    .clip(iconShape)
                                    .background(iconBackground),
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier.size(72.dp)
                                .clip(iconShape)
                                .background(iconBackground),
                    ) {
                        EqualizerWaveBars(
                            isAnimating = uiState.dsOn,
                            tint = iconTint,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text =
                            if (uiState.dsOn) {
                                stringResource(R.string.dolby_atmos_on)
                            } else {
                                stringResource(R.string.dolby_atmos_off)
                            },
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = titleColor,
                    )
                    if (uiState.dsOn) {
                        val summary =
                            if (uiState.profileName.isNotEmpty()) {
                                "${uiState.profileName} • ${uiState.presetName}"
                            } else {
                                uiState.presetName
                            }
                        Surface(
                            shape = MaterialShapes.Pill.toShape(),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = summary,
                                style =
                                    MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                    ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.dolby_off),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Switch(
                checked = uiState.dsOn,
                onCheckedChange = null,
                thumbContent =
                    if (uiState.dsOn) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else null,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            )
        }
    }
}

/** Profile selector row with distinct polygon badges and smooth corner morphing. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileSection(
    uiState: DolbyUiState,
    onSelectProfile: (Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    val haptics = rememberDolbyHaptics()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.dolby_profile_title),
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() },
        )

        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .selectableGroup()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement =
                Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            ProfileIds.forEachIndexed { index, profileId ->
                val isSelected = uiState.profile == profileId

                val targetTopStart = if (isSelected) 24.dp else if (index == 0) 16.dp else 6.dp
                val targetBottomStart = if (isSelected) 24.dp else if (index == 0) 16.dp else 6.dp
                val targetTopEnd =
                    if (isSelected) 24.dp else if (index == ProfileIds.lastIndex) 16.dp else 6.dp
                val targetBottomEnd =
                    if (isSelected) 24.dp else if (index == ProfileIds.lastIndex) 16.dp else 6.dp

                val animatedTopStart by
                    animateDpAsState(
                        targetTopStart,
                        expressiveFastSpatialSpec(),
                        label = "ProfileTopStart_$index",
                    )
                val animatedBottomStart by
                    animateDpAsState(
                        targetBottomStart,
                        expressiveFastSpatialSpec(),
                        label = "ProfileBotStart_$index",
                    )
                val animatedTopEnd by
                    animateDpAsState(
                        targetTopEnd,
                        expressiveFastSpatialSpec(),
                        label = "ProfileTopEnd_$index",
                    )
                val animatedBottomEnd by
                    animateDpAsState(
                        targetBottomEnd,
                        expressiveFastSpatialSpec(),
                        label = "ProfileBotEnd_$index",
                    )

                val buttonShape =
                    RoundedCornerShape(
                        topStart = animatedTopStart,
                        bottomStart = animatedBottomStart,
                        topEnd = animatedTopEnd,
                        bottomEnd = animatedBottomEnd,
                    )

                ToggleButton(
                    checked = isSelected,
                    onCheckedChange = {
                        haptics.selectionClick()
                        onSelectProfile(profileId)
                    },
                    enabled = uiState.dsOn,
                    shapes = ToggleButtonDefaults.shapes(shape = buttonShape),
                    colors =
                        ToggleButtonDefaults.toggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    modifier =
                        Modifier.defaultMinSize(minWidth = 72.dp, minHeight = 48.dp)
                            .semantics { role = Role.RadioButton },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 4.dp),
                    ) {
                        val profileShape = ProfilePolygons[index].toShape()
                        Box(
                            modifier =
                                Modifier.size(16.dp)
                                    .clip(profileShape)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
                                        } else {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                        }
                                    ),
                        )
                        Text(
                            text = stringResource(ProfileLabels[index]),
                            style = MaterialTheme.typography.labelLargeEmphasized,
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.profile) {
        val selectedIndex = ProfileIds.indexOf(uiState.profile).coerceAtLeast(0)
        val approxItemWidth = 110
        scrollState.animateScrollTo(selectedIndex * approxItemWidth)
    }
}

/** Intelligent Equalizer selector row with vibrant tonal accents and tactile touch morphing. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IeqSection(
    uiState: DolbyUiState,
    onSelectIeq: (Int) -> Unit,
) {
    val outerShape = RoundedCornerShape(28.dp)
    val fontScale = LocalConfiguration.current.fontScale
    val isLargeFont = fontScale > 1.15f
    val haptics = rememberDolbyHaptics()

    Card(
        shape = outerShape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.dolby_ieq),
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp).semantics { heading() },
            )

            val activeContainerColors =
                listOf(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.primaryContainer,
                )
            val activeContentColors =
                listOf(
                    MaterialTheme.colorScheme.onSurface,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                )

            val contentRow: @Composable () -> Unit = {
                IeqValues.forEachIndexed { index, value ->
                    val isSelected = uiState.ieqPreset == value
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val targetTopStart = if (isSelected) 24.dp else if (index == 0) 16.dp else 6.dp
                    val targetBottomStart = if (isSelected) 24.dp else if (index == 0) 16.dp else 6.dp
                    val targetTopEnd =
                        if (isSelected) 24.dp else if (index == IeqValues.lastIndex) 16.dp else 6.dp
                    val targetBottomEnd =
                        if (isSelected) 24.dp else if (index == IeqValues.lastIndex) 16.dp else 6.dp

                    val animatedTopStart by
                        animateDpAsState(
                            targetTopStart,
                            expressiveFastSpatialSpec(),
                            label = "IeqTopStart_$index",
                        )
                    val animatedBottomStart by
                        animateDpAsState(
                            targetBottomStart,
                            expressiveFastSpatialSpec(),
                            label = "IeqBotStart_$index",
                        )
                    val animatedTopEnd by
                        animateDpAsState(
                            targetTopEnd,
                            expressiveFastSpatialSpec(),
                            label = "IeqTopEnd_$index",
                        )
                    val animatedBottomEnd by
                        animateDpAsState(
                            targetBottomEnd,
                            expressiveFastSpatialSpec(),
                            label = "IeqBotEnd_$index",
                        )

                    val buttonShape =
                        RoundedCornerShape(
                            topStart = animatedTopStart,
                            bottomStart = animatedBottomStart,
                            topEnd = animatedTopEnd,
                            bottomEnd = animatedBottomEnd,
                        )

                    val morphTarget = if (isPressed) 1f else 0f
                    val morphProgress by
                        animateFloatAsState(
                            targetValue = morphTarget,
                            animationSpec = expressiveFastSpatialSpec(),
                            label = "IeqMorphProgress_$index",
                        )
                    val morph = IeqMorphs[index]
                    val badgeShape =
                        if (morph != null) {
                            remember(morphProgress) { MorphShape(morph, morphProgress) }
                        } else {
                            IeqPolygons[index].toShape()
                        }

                    val pressScale by
                        animateFloatAsState(
                            targetValue = if (isPressed) 0.94f else 1.0f,
                            animationSpec = expressiveFastSpatialSpec(),
                            label = "IeqPressScale_$index",
                        )

                    ToggleButton(
                        checked = isSelected,
                        onCheckedChange = {
                            haptics.selectionClick()
                            onSelectIeq(value)
                        },
                        interactionSource = interactionSource,
                        enabled = uiState.isProfileActive,
                        shapes = ToggleButtonDefaults.shapes(shape = buttonShape),
                        colors =
                            ToggleButtonDefaults.toggleButtonColors(
                                checkedContainerColor = activeContainerColors[index],
                                checkedContentColor = activeContentColors[index],
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        modifier =
                            Modifier.then(
                                if (!isLargeFont) Modifier.weight(1f) else Modifier.defaultMinSize(minWidth = 72.dp)
                            )
                                .defaultMinSize(minHeight = 64.dp)
                                .graphicsLayer {
                                    scaleX = pressScale
                                    scaleY = pressScale
                                }
                                .semantics { role = Role.RadioButton },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(24.dp)
                                        .clip(badgeShape)
                                        .background(
                                            if (isSelected) {
                                                activeContentColors[index].copy(alpha = 0.2f)
                                            } else {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            }
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(IeqIcons[index]),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint =
                                        if (isSelected) {
                                            activeContentColors[index]
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(IeqLabels[index]),
                                style = MaterialTheme.typography.labelSmallEmphasized,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
            }

            if (!isLargeFont) {
                Row(
                    modifier = Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement =
                        Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    contentRow()
                }
            } else {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .selectableGroup()
                            .padding(vertical = 4.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    contentRow()
                }
            }
        }
    }
}

/** Miniature 10-node sparkline preview curve of the active preset. */
@Composable
private fun EqualizerSparkline(
    gains: List<Int>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val lineColor = if (enabled) primaryColor else outlineVariant.copy(alpha = 0.5f)
    val fillColor = if (enabled) primaryColor.copy(alpha = 0.22f) else Color.Transparent

    Canvas(modifier = modifier) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val normalizedGains = if (gains.size >= 10) gains.take(10) else List(10) { 0 }
        val points = mutableListOf<Offset>()
        val paddingY = 4.dp.toPx()
        val availableHeight = size.height - 2 * paddingY
        val centerY = size.height / 2f
        val stepX = size.width / 9f

        normalizedGains.forEachIndexed { i, gain ->
            val x = i * stepX
            val clampedGain = gain.coerceIn(-100, 100).toFloat()
            val y = centerY - (clampedGain / 100f) * (availableHeight / 2f)
            points.add(Offset(x, y.coerceIn(paddingY, size.height - paddingY)))
        }

        val curvePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p0 = if (i > 0) points[i - 1] else Offset(points[i].x - stepX, points[i].y)
                val p1 = points[i]
                val p2 = points[i + 1]
                val p3 =
                    if (i + 2 < points.size) {
                        points[i + 2]
                    } else {
                        Offset(points[i + 1].x + stepX, points[i + 1].y)
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
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush =
                Brush.verticalGradient(
                    colors = listOf(fillColor, Color.Transparent),
                    startY = paddingY,
                    endY = size.height,
                ),
        )

        drawPath(
            path = curvePath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        points.forEach { pt ->
            drawCircle(
                color = lineColor,
                radius = 1.75.dp.toPx(),
                center = pt,
            )
        }
    }
}

/** Equalizer navigation entry with live sparkline preview and press micro-animation. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EqualizerEntryCard(
    uiState: DolbyUiState,
    context: Context,
) {
    val haptics = rememberDolbyHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1.0f,
            animationSpec = expressiveFastSpatialSpec(),
            label = "EqCardScale",
        )

    Surface(
        onClick = {
            haptics.selectionClick()
            context.startActivity(Intent(context, EqualizerActivity::class.java))
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(28.dp),
        enabled = uiState.isProfileActive,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.dolby_preset),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color =
                        if (uiState.isProfileActive) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                )
                Surface(
                    shape = MaterialShapes.Pill.toShape(),
                    color =
                        if (uiState.isProfileActive) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                ) {
                    Text(
                        text = uiState.presetName,
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        color =
                            if (uiState.isProfileActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EqualizerSparkline(
                    gains = uiState.presetGains,
                    enabled = uiState.isProfileActive,
                    modifier = Modifier.width(96.dp).height(36.dp),
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.dolby_preset),
                    tint =
                        if (uiState.isProfileActive) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        },
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

/** Enhancements section with toggles and discrete sliders. */
@Composable
private fun EnhancementsSection(
    uiState: DolbyUiState,
    viewModel: DolbyViewModel,
) {
    val outerShape = RoundedCornerShape(28.dp)
    val firstShape =
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp,
        )
    val middleShape = RoundedCornerShape(4.dp)
    val lastShape =
        RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp,
        )

    Card(
        shape = outerShape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.dolby_enhancements_title),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1.sp,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        .semantics { heading() },
            )

            SwitchRowItem(
                title = stringResource(R.string.dolby_bass_enhancer),
                checked = uiState.bassEnabled,
                enabled = uiState.isProfileActive,
                onCheckedChange = { viewModel.setBassEnhancerEnabled(it) },
                shape = firstShape,
            )

            SwitchRowItem(
                title = stringResource(R.string.dolby_volume_leveler),
                checked = uiState.volumeLevelerEnabled,
                enabled = uiState.isProfileActive,
                onCheckedChange = { viewModel.setVolumeLevelerEnabled(it) },
                shape = middleShape,
            )

            SwitchRowItem(
                title = stringResource(R.string.dolby_spk_virtualizer),
                checked = uiState.spkVirtEnabled,
                enabled = uiState.isProfileActive,
                onCheckedChange = { viewModel.setSpeakerVirtEnabled(it) },
                shape = middleShape,
            )

            val hpGatingMessage =
                if (uiState.isOnSpeaker) {
                    stringResource(R.string.dolby_connect_headphones)
                } else null
            SwitchRowItem(
                title = stringResource(R.string.dolby_hp_virtualizer),
                subtitle = hpGatingMessage,
                checked = uiState.hpVirtEnabled,
                enabled = uiState.isHeadphoneVirtAvailable,
                onCheckedChange = { viewModel.setHeadphoneVirtEnabled(it) },
                shape = middleShape,
            )

            DiscreteSliderItem(
                title = stringResource(R.string.dolby_dialogue_enhancer),
                currentIndex = dialogueValueToIndex(uiState.dialogueAmount),
                stepsCount = 3,
                maxIndex = 4,
                labels = DialogueLabels,
                enabled = uiState.isProfileActive,
                onIndexChanged = { idx ->
                    val value = DialogueValues.getOrElse(idx) { 0 }
                    viewModel.setDialogueEnhancerAmount(value)
                },
                shape = if (uiState.stereoWideningSupported) middleShape else lastShape,
            )

            if (uiState.stereoWideningSupported) {
                val stereoGatingMessage =
                    when {
                        uiState.isOnSpeaker -> stringResource(R.string.dolby_connect_headphones)
                        !uiState.hpVirtEnabled ->
                            stringResource(R.string.dolby_stereo_widening_requires_virtualizer)
                        else -> null
                    }
                DiscreteSliderItem(
                    title = stringResource(R.string.dolby_stereo_widening),
                    subtitle = stereoGatingMessage,
                    currentIndex = stereoValueToIndex(uiState.stereoAmount),
                    stepsCount = 2,
                    maxIndex = 3,
                    labels = StereoLabels,
                    enabled = uiState.isStereoWideningAvailable,
                    onIndexChanged = { idx ->
                        val value = StereoValues.getOrElse(idx) { 4 }
                        viewModel.setStereoWideningAmount(value)
                    },
                    shape = lastShape,
                )
            }
        }
    }
}

/** Switch row with merged accessibility semantics. */
@Composable
private fun SwitchRowItem(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: RoundedCornerShape,
    subtitle: String? = null,
) {
    val haptics = rememberDolbyHaptics()
    Surface(
        checked = checked,
        onCheckedChange = { newState ->
            if (newState) haptics.confirm() else haptics.reject()
            onCheckedChange(newState)
        },
        enabled = enabled,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier =
            Modifier.fillMaxWidth().semantics {
                role = Role.Switch
                if (!enabled && subtitle != null) {
                    stateDescription = subtitle
                }
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier =
                Modifier.fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = null,
                thumbContent =
                    if (checked) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    },
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            )
        }
    }
}

/** Stepped discrete slider item. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DiscreteSliderItem(
    title: String,
    currentIndex: Int,
    stepsCount: Int,
    maxIndex: Int,
    labels: List<Int>,
    enabled: Boolean,
    onIndexChanged: (Int) -> Unit,
    shape: RoundedCornerShape,
    subtitle: String? = null,
) {
    val haptics = rememberDolbyHaptics()
    val currentOnIndexChanged by rememberUpdatedState(onIndexChanged)
    val sliderState =
        remember(currentIndex, stepsCount, maxIndex) {
            SliderState(
                value = currentIndex.toFloat(),
                steps = stepsCount,
                valueRange = 0f..maxIndex.toFloat(),
            )
        }

    sliderState.onValueChangeFinished = {
        currentOnIndexChanged(sliderState.value.roundToInt())
    }

    LaunchedEffect(currentIndex) {
        if (sliderState.value.roundToInt() != currentIndex) {
            sliderState.value = currentIndex.toFloat()
        }
    }

    var lastHapticStep by remember(currentIndex) { mutableIntStateOf(currentIndex) }

    LaunchedEffect(sliderState.value) {
        val currentStep = sliderState.value.roundToInt()
        if (currentStep != lastHapticStep) {
            haptics.stepTick()
            lastHapticStep = currentStep
        }
    }

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color =
                            if (enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            },
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                val currentLabel =
                    stringResource(labels.getOrElse(sliderState.value.roundToInt()) { labels[0] })
                Text(
                    text = currentLabel,
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontFeatureSettings = TabularNumerals,
                        ),
                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        },
                )
            }

            Slider(
                state = sliderState,
                enabled = enabled,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .semantics {
                            stateDescription =
                                if (!enabled && subtitle != null) {
                                    subtitle
                                } else {
                                    ""
                                }
                        },
            )
        }
    }
}

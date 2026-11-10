/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.ui.theme

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * CompositionLocal indicating whether system animations are disabled.
 *
 * Sourced dynamically from [Settings.Global.ANIMATOR_DURATION_SCALE].
 * When true, all motion snaps instantly (zero duration, no spatial overshoot)
 * and decorative loops or pulses are halted.
 */
val LocalReduceMotion = compositionLocalOf { false }

/**
 * Collapses spatial and effects specs to [snap] when reduced motion is enabled.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Immutable
object SnapMotionScheme : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = snap()
}

/**
 * Observes system animation duration scales using a [ContentObserver].
 * Returns a [State] that updates immediately whenever the accessibility setting changes.
 */
@Composable
fun rememberReduceMotionState(): State<Boolean> {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val reduceMotionState = remember {
        mutableStateOf(isReduceMotionActive(context))
    }

    DisposableEffect(resolver) {
        val uriAnimator = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    reduceMotionState.value = isReduceMotionActive(context)
                }
            }
        resolver.registerContentObserver(uriAnimator, false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return reduceMotionState
}

private fun isReduceMotionActive(context: Context): Boolean {
    val resolver = context.contentResolver
    val durationScale =
        Settings.Global.getFloat(
            resolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f,
        )
    return durationScale == 0.0f
}

/**
 * Default spatial spring spec for physical movement, bounds resizing, and shape morphs.
 * Delegates to [MaterialTheme.motionScheme.defaultSpatialSpec] or collapses to [snap].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ReadOnlyComposable
fun <T> expressiveDefaultSpatialSpec(): FiniteAnimationSpec<T> {
    return if (LocalReduceMotion.current) {
        snap()
    } else {
        MaterialTheme.motionScheme.defaultSpatialSpec()
    }
}

/**
 * Backward-compatible alias for [expressiveDefaultSpatialSpec].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ReadOnlyComposable
fun <T> spatialMotionSpec(): FiniteAnimationSpec<T> = expressiveDefaultSpatialSpec()

/**
 * Fast spatial spring spec for small component interactions like slider touches.
 * Delegates to [MaterialTheme.motionScheme.fastSpatialSpec] or collapses to [snap].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ReadOnlyComposable
fun <T> expressiveFastSpatialSpec(): FiniteAnimationSpec<T> {
    return if (LocalReduceMotion.current) {
        snap()
    } else {
        MaterialTheme.motionScheme.fastSpatialSpec()
    }
}

/**
 * Slow spatial spring spec for large full-screen or sheet transitions.
 * Delegates to [MaterialTheme.motionScheme.slowSpatialSpec] or collapses to [snap].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ReadOnlyComposable
fun <T> expressiveSlowSpatialSpec(): FiniteAnimationSpec<T> {
    return if (LocalReduceMotion.current) {
        snap()
    } else {
        MaterialTheme.motionScheme.slowSpatialSpec()
    }
}

/**
 * Default effects spring spec for non-overshooting color, opacity, and tonal transitions.
 * Delegates to [MaterialTheme.motionScheme.defaultEffectsSpec] or collapses to [snap].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ReadOnlyComposable
fun <T> expressiveDefaultEffectsSpec(): FiniteAnimationSpec<T> {
    return if (LocalReduceMotion.current) {
        snap()
    } else {
        MaterialTheme.motionScheme.defaultEffectsSpec()
    }
}

/**
 * Fast effects spring spec for rapid non-overshooting feedback transitions.
 * Delegates to [MaterialTheme.motionScheme.fastEffectsSpec] or collapses to [snap].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ReadOnlyComposable
fun <T> expressiveFastEffectsSpec(): FiniteAnimationSpec<T> {
    return if (LocalReduceMotion.current) {
        snap()
    } else {
        MaterialTheme.motionScheme.fastEffectsSpec()
    }
}

/**
 * Slow effects spring spec for extended non-overshooting background shifts.
 * Delegates to [MaterialTheme.motionScheme.slowEffectsSpec] or collapses to [snap].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ReadOnlyComposable
fun <T> expressiveSlowEffectsSpec(): FiniteAnimationSpec<T> {
    return if (LocalReduceMotion.current) {
        snap()
    } else {
        MaterialTheme.motionScheme.slowEffectsSpec()
    }
}

/**
 * Tactile feedback bridge for settings toggles and slider interactions.
 */
@Immutable
class DolbyHaptics(private val view: View) {
    /** Precision tick for increments and slider steps. */
    fun stepTick() {
        view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
    }

    /** Tactile detent when crossing the neutral 0 dB notch. */
    fun centerDetent() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /** Strong confirmation feedback for state activations. */
    fun confirm() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    /** Strong rejection feedback for deactivations. */
    fun reject() {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    /** Light tap for option selection. */
    fun selectionClick() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /** Subtle tactile tick for micro-interactions and touch releases. */
    fun pulseTick() {
        view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
    }
}

/** Remembers a [DolbyHaptics] instance bound to the current view. */
@Composable
fun rememberDolbyHaptics(): DolbyHaptics {
    val view = LocalView.current
    return remember(view) { DolbyHaptics(view) }
}

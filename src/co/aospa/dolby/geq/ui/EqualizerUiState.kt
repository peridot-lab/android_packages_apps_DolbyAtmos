/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.geq.ui

import androidx.compose.runtime.Immutable
import co.aospa.dolby.geq.data.BandGain
import co.aospa.dolby.geq.data.Preset

/** UI state for the Equalizer screen. */
@Immutable
data class EqualizerUiState(
    val presets: List<Preset> = emptyList(),
    val currentPreset: Preset = Preset(name = "Flat", bandGains = emptyList()),
    val selectedBandIndex: Int = 0,
    val isPresetRestored: Boolean = false,
) {
    val bandGains: List<BandGain>
        get() = currentPreset.bandGains

    val isUserDefined: Boolean
        get() = currentPreset.isUserDefined

    val isMutated: Boolean
        get() = currentPreset.isMutated
}

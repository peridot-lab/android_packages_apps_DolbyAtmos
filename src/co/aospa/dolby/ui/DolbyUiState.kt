/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.ui

import androidx.compose.runtime.Immutable
import co.aospa.dolby.data.DolbyModel

/**
 * Immutable UI state for the Dolby Atmos main screen.
 * Wraps [DolbyModel] directly without duplicating state fields.
 */
@Immutable
data class DolbyUiState(
    val model: DolbyModel = DolbyModel(),
) {
    val dsOn: Boolean get() = model.dsOn
    val profile: Int get() = model.profile
    val profileName: String get() = model.profileName
    val presetName: String get() = model.presetName
    val presetGains: List<Int> get() = model.presetGains.ifEmpty { List(10) { 0 } }
    val ieqPreset: Int get() = model.ieqPreset
    val dialogueAmount: Int get() = model.dialogueAmount
    val bassEnabled: Boolean get() = model.bassEnabled
    val volumeLevelerEnabled: Boolean get() = model.volumeLevelerEnabled
    val hpVirtEnabled: Boolean get() = model.hpVirtEnabled
    val spkVirtEnabled: Boolean get() = model.spkVirtEnabled
    val stereoAmount: Int get() = model.stereoAmount
    val isOnSpeaker: Boolean get() = model.isOnSpeaker
    val stereoWideningSupported: Boolean get() = model.stereoWideningSupported

    /** Whether profile-specific settings are accessible. */
    val isProfileActive: Boolean
        get() = dsOn && profile != -1

    /** Headphone virtualization is available when connected to headphones. */
    val isHeadphoneVirtAvailable: Boolean
        get() = isProfileActive && !isOnSpeaker

    /** Stereo widening requires headphone virtualization enabled while on headphones. */
    val isStereoWideningAvailable: Boolean
        get() = isProfileActive && !isOnSpeaker && stereoWideningSupported && hpVirtEnabled
}

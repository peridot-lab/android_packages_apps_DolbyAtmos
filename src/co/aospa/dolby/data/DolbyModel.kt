/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.data

/**
 * Data model representing the internal state of Dolby Atmos audio effect and hardware routing.
 */
data class DolbyModel(
    val dsOn: Boolean = true,
    val profile: Int = 0,
    val profileName: String = "",
    val presetName: String = "",
    val presetGains: List<Int> = emptyList(),
    val ieqPreset: Int = 0,
    val dialogueAmount: Int = 0,
    val bassEnabled: Boolean = false,
    val volumeLevelerEnabled: Boolean = false,
    val hpVirtEnabled: Boolean = false,
    val spkVirtEnabled: Boolean = false,
    val stereoAmount: Int = 0,
    val isOnSpeaker: Boolean = true,
    val stereoWideningSupported: Boolean = false,
)

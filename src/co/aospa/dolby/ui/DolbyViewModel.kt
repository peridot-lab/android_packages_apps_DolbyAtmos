/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.aospa.dolby.data.DolbyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Dolby Atmos main screen.
 * Exposes immutable [DolbyUiState] via [uiState] and unidirectional action handlers.
 */
class DolbyViewModel(private val repository: DolbyRepository) : ViewModel() {

    val uiState: StateFlow<DolbyUiState> =
        repository.dolbyState
            .map { DolbyUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DolbyUiState(repository.dolbyState.value),
            )

    fun refresh() {
        viewModelScope.launch { repository.refresh() }
    }

    fun setDolbyEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setDolbyEnabled(enabled) }
    }

    fun setProfile(profile: Int) {
        viewModelScope.launch { repository.setProfile(profile) }
    }

    fun setIeqPreset(preset: Int) {
        viewModelScope.launch { repository.setIeqPreset(preset) }
    }

    fun setDialogueEnhancerAmount(amount: Int) {
        viewModelScope.launch { repository.setDialogueEnhancerAmount(amount) }
    }

    fun setBassEnhancerEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setBassEnhancerEnabled(enabled) }
    }

    fun setVolumeLevelerEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setVolumeLevelerEnabled(enabled) }
    }

    fun setHeadphoneVirtEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHeadphoneVirtEnabled(enabled) }
    }

    fun setSpeakerVirtEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSpeakerVirtEnabled(enabled) }
    }

    fun setStereoWideningAmount(amount: Int) {
        viewModelScope.launch { repository.setStereoWideningAmount(amount) }
    }

    fun resetProfileSettings() {
        viewModelScope.launch { repository.resetProfileSettings() }
    }
}

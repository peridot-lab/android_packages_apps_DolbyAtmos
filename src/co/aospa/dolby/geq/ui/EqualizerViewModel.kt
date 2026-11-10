/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.geq.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.aospa.dolby.DolbyConstants.Companion.dlog
import co.aospa.dolby.geq.data.EqualizerRepository
import co.aospa.dolby.geq.data.Preset
import co.aospa.dolby.ui.DolbyViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val TAG = "EqViewModel"

class EqualizerViewModel(private val repository: EqualizerRepository) : ViewModel() {

    private val _presets = MutableStateFlow(repository.builtInPresets)
    val presets = _presets.asStateFlow()

    private val _preset = MutableStateFlow(repository.defaultPreset)
    val preset = _preset.asStateFlow()

    private val _selectedBandIndex = MutableStateFlow(0)
    val selectedBandIndex: StateFlow<Int> = _selectedBandIndex.asStateFlow()

    private val _isPresetRestored = MutableStateFlow(false)
    val isPresetRestored: StateFlow<Boolean> = _isPresetRestored.asStateFlow()

    val uiState: StateFlow<EqualizerUiState> =
        combine(_presets, _preset, _selectedBandIndex, _isPresetRestored) { presetsList, activePreset, selectedIndex, restored ->
            EqualizerUiState(
                presets = presetsList,
                currentPreset = activePreset,
                selectedBandIndex = selectedIndex,
                isPresetRestored = restored,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue =
                EqualizerUiState(
                    presets = repository.builtInPresets,
                    currentPreset = repository.defaultPreset,
                    selectedBandIndex = 0,
                    isPresetRestored = false,
                ),
        )

    init {
        // Update the list of presets: combined list of user defined presets if any,
        // and then the built in presets.
        repository.userPresets
            .onEach { presets ->
                dlog(TAG, "updated userPresets: $presets")
                _presets.value =
                    mutableListOf<Preset>()
                        .apply {
                            addAll(presets)
                            addAll(repository.builtInPresets)
                        }
                        .toList()

                // We can restore the active preset only after the presets list is populated,
                // since we do not save the preset name but only its gains.
                if (!_isPresetRestored.value) {
                    val bandGains = repository.getBandGains()
                    _preset.value =
                        _presets.value.find { bandGains == it.bandGains }
                            ?: Preset(name = "Custom", bandGains = bandGains)
                    dlog(TAG, "restored preset: ${_preset.value}")
                    _isPresetRestored.value = true
                }
            }
            .launchIn(viewModelScope)

        // Update the preset in repository everytime we set it here
        _preset
            .drop(1) // skip the initial value
            .onEach {
                // wait till the active preset is restored
                if (!_isPresetRestored.value) {
                    return@onEach
                }
                dlog(TAG, "updated preset: $it")
                repository.setBandGains(it.bandGains)
                if (it.isUserDefined) {
                    repository.addPreset(it)
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectBand(index: Int) {
        if (index in 0 until 10) {
            _selectedBandIndex.value = index
        }
    }

    fun reset() {
        dlog(TAG, "reset()")
        if (_preset.value.isUserDefined) {
            _preset.value = _preset.value.copy(bandGains = repository.defaultPreset.bandGains)
        } else {
            _preset.value = repository.defaultPreset
        }
    }

    fun setPreset(preset: Preset) {
        dlog(TAG, "setPreset($preset)")
        _preset.value = preset
    }

    fun setGain(index: Int, gain: Int) {
        dlog(TAG, "setGain($index, $gain)")
        _preset.value =
            _preset.value.run {
                copy(
                    name = if (!isUserDefined) "Custom" else name,
                    bandGains =
                        bandGains
                            .toMutableList()
                            .apply { this[index] = this[index].copy(gain = gain) }
                            .toList(),
                    isMutated = true,
                )
            }
    }

    private fun validatePresetName(name: String): PresetNameValidationError? {
        return if (_presets.value.any { it.name.equals(name.trim(), ignoreCase = true) }) {
            PresetNameValidationError.NAME_EXISTS
        } else if (name.length > 50) {
            PresetNameValidationError.NAME_TOO_LONG
        } else null
    }

    fun createNewPreset(name: String): PresetNameValidationError? {
        dlog(TAG, "createNewPreset($name)")
        validatePresetName(name)?.let {
            dlog(TAG, "createNewPreset failed: $it")
            return it
        }
        _preset.value =
            _preset.value.copy(name = name.trim(), isUserDefined = true, isMutated = false)
        return null
    }

    fun renamePreset(preset: Preset, name: String): PresetNameValidationError? {
        dlog(TAG, "renamePreset($preset, $name)")
        createNewPreset(name = name)?.let {
            dlog(TAG, "renamePreset failed")
            return it
        }
        deletePreset(preset, shouldReset = false)
        return null
    }

    fun deletePreset(preset: Preset, shouldReset: Boolean = true) {
        dlog(TAG, "deletePreset($preset)")
        viewModelScope.launch { repository.removePreset(preset) }
        if (shouldReset) {
            _preset.value = repository.defaultPreset
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                DolbyViewModelFactory.getInstance(app).create(EqualizerViewModel::class.java)
            }
        }
    }
}

/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.data

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import co.aospa.dolby.DolbyConstants
import co.aospa.dolby.DolbyController
import co.aospa.dolby.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Repository managing Dolby Atmos audio state, device routing, and preferences.
 * Controller calls are serialized through [DolbyControllerGate] on [Dispatchers.Main.immediate].
 */
class DolbyRepository private constructor(private val context: Context) {

    private val dolbyController = DolbyController.getInstance(context)
    private val audioManager = context.getSystemService(AudioManager::class.java)!!

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val stereoWideningSupported: Boolean =
        context.resources.getBoolean(R.bool.dolby_stereo_widening_supported)

    private fun getDefaultPrefs(): SharedPreferences =
        context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

    private fun getProfilePrefs(profile: Int): SharedPreferences =
        context.getSharedPreferences("profile_$profile", Context.MODE_PRIVATE)

    private val _isOnSpeaker = MutableStateFlow(true)
    val isOnSpeaker: StateFlow<Boolean> = _isOnSpeaker.asStateFlow()

    private val _controllerModel = MutableStateFlow(readControllerStateSafe())

    /** Flow observing audio device routing changes. */
    private val deviceCallbackFlow: Flow<Boolean> = callbackFlow {
        val checkSpeaker = {
            try {
                val devices = audioManager.getDevicesForAttributes(ATTRIBUTES_MEDIA)
                val onSpeaker =
                    if (devices.isNotEmpty()) {
                        devices[0].type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    } else {
                        true
                    }
                trySend(onSpeaker)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking audio devices", e)
                trySend(true)
            }
        }

        val callback =
            object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                    checkSpeaker()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
                    checkSpeaker()
                }
            }

        audioManager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        checkSpeaker()

        awaitClose {
            audioManager.unregisterAudioDeviceCallback(callback)
        }
    }

    /** Flow observing preference changes from external components. */
    private val prefsChangeFlow: Flow<Unit> = callbackFlow {
        val defaultPrefs = getDefaultPrefs()
        var activeProfile = runCatching { dolbyController.profile }.getOrDefault(0)
        var profilePrefs: SharedPreferences? = getProfilePrefs(activeProfile)

        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                val newProfile = runCatching { dolbyController.profile }.getOrDefault(0)
                if (newProfile != activeProfile) {
                    profilePrefs?.unregisterOnSharedPreferenceChangeListener(null)
                    activeProfile = newProfile
                    profilePrefs = getProfilePrefs(newProfile)
                    profilePrefs?.registerOnSharedPreferenceChangeListener(null)
                }
                trySend(Unit)
            }

        defaultPrefs.registerOnSharedPreferenceChangeListener(listener)
        profilePrefs?.registerOnSharedPreferenceChangeListener(listener)
        trySend(Unit)

        awaitClose {
            defaultPrefs.unregisterOnSharedPreferenceChangeListener(listener)
            profilePrefs?.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    /** StateFlow providing the combined Dolby model. */
    val dolbyState: StateFlow<DolbyModel> =
        combine(
            _controllerModel,
            _isOnSpeaker,
            deviceCallbackFlow.catch { e -> Log.e(TAG, "Device callback stream error", e) },
            prefsChangeFlow.catch { e -> Log.e(TAG, "Prefs change stream error", e) },
        ) { model, _, onSpeaker, _ ->
            _isOnSpeaker.value = onSpeaker
            model.copy(isOnSpeaker = onSpeaker)
        }.stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = readControllerStateSafe().copy(isOnSpeaker = true),
        )

    private fun readControllerStateSafe(): DolbyModel {
        return try {
            val dsOn = dolbyController.dsOn
            val profile = dolbyController.profile
            val profileName = dolbyController.getProfileName() ?: ""
            val presetName = dolbyController.getPresetName()
            val presetGains =
                try {
                    dolbyController.getPreset().split(",").mapNotNull { it.trim().toIntOrNull() }
                } catch (e: Exception) {
                    emptyList()
                }
            val ieqPreset = dolbyController.getIeqPreset()
            val dialogueAmount = dolbyController.getDialogueEnhancerAmount()
            val bassEnabled = dolbyController.getBassEnhancerEnabled()
            val volumeLevelerEnabled = dolbyController.getVolumeLevelerEnabled()
            val hpVirtEnabled = dolbyController.getHeadphoneVirtEnabled()
            val spkVirtEnabled = dolbyController.getSpeakerVirtEnabled()
            val stereoAmount = dolbyController.getStereoWideningAmount()

            DolbyModel(
                dsOn = dsOn,
                profile = profile,
                profileName = profileName,
                presetName = presetName,
                presetGains = presetGains,
                ieqPreset = ieqPreset,
                dialogueAmount = dialogueAmount,
                bassEnabled = bassEnabled,
                volumeLevelerEnabled = volumeLevelerEnabled,
                hpVirtEnabled = hpVirtEnabled,
                spkVirtEnabled = spkVirtEnabled,
                stereoAmount = stereoAmount,
                isOnSpeaker = _isOnSpeaker.value,
                stereoWideningSupported = stereoWideningSupported,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error reading DolbyController state", e)
            DolbyModel(stereoWideningSupported = stereoWideningSupported)
        }
    }

    suspend fun refresh() {
        DolbyControllerGate.execute {
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun setDolbyEnabled(enabled: Boolean) {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.dsOn = enabled
                getDefaultPrefs()
                    .edit()
                    .putBoolean(DolbyConstants.PREF_ENABLE, enabled)
                    .apply()
            }.onFailure { Log.e(TAG, "Failed to set Dolby enabled", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun setProfile(profile: Int) {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.profile = profile
                getDefaultPrefs()
                    .edit()
                    .putString(DolbyConstants.PREF_PROFILE, profile.toString())
                    .apply()
            }.onFailure { Log.e(TAG, "Failed to set profile", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun setIeqPreset(preset: Int) {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.setIeqPreset(preset)
                val currentProfile = dolbyController.profile
                getProfilePrefs(currentProfile)
                    .edit()
                    .putString(DolbyConstants.PREF_IEQ, preset.toString())
                    .apply()
            }.onFailure { Log.e(TAG, "Failed to set IEQ preset", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun setDialogueEnhancerAmount(amount: Int) {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.setDialogueEnhancerAmount(amount)
                val currentProfile = dolbyController.profile
                getProfilePrefs(currentProfile)
                    .edit()
                    .putString(DolbyConstants.PREF_DIALOGUE, amount.toString())
                    .apply()
            }.onFailure { Log.e(TAG, "Failed to set dialogue enhancer amount", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun setBassEnhancerEnabled(enabled: Boolean) {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.setBassEnhancerEnabled(enabled)
                val currentProfile = dolbyController.profile
                getProfilePrefs(currentProfile)
                    .edit()
                    .putBoolean(DolbyConstants.PREF_BASS, enabled)
                    .apply()
            }.onFailure { Log.e(TAG, "Failed to set bass enhancer enabled", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun setVolumeLevelerEnabled(enabled: Boolean) {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.setVolumeLevelerEnabled(enabled)
                val currentProfile = dolbyController.profile
                getProfilePrefs(currentProfile)
                    .edit()
                    .putBoolean(DolbyConstants.PREF_VOLUME, enabled)
                    .apply()
            }.onFailure { Log.e(TAG, "Failed to set volume leveler enabled", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun setHeadphoneVirtEnabled(enabled: Boolean) {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.setHeadphoneVirtEnabled(enabled)
                val currentProfile = dolbyController.profile
                getProfilePrefs(currentProfile)
                    .edit()
                    .putBoolean(DolbyConstants.PREF_HP_VIRTUALIZER, enabled)
                    .apply()
            }.onFailure { Log.e(TAG, "Failed to set headphone virt enabled", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun setSpeakerVirtEnabled(enabled: Boolean) {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.setSpeakerVirtEnabled(enabled)
                val currentProfile = dolbyController.profile
                getProfilePrefs(currentProfile)
                    .edit()
                    .putBoolean(DolbyConstants.PREF_SPK_VIRTUALIZER, enabled)
                    .apply()
            }.onFailure { Log.e(TAG, "Failed to set speaker virt enabled", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun setStereoWideningAmount(amount: Int) {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.setStereoWideningAmount(amount)
                val currentProfile = dolbyController.profile
                getProfilePrefs(currentProfile)
                    .edit()
                    .putString(DolbyConstants.PREF_STEREO, amount.toString())
                    .apply()
            }.onFailure { Log.e(TAG, "Failed to set stereo widening amount", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    suspend fun resetProfileSettings() {
        DolbyControllerGate.execute {
            runCatching {
                dolbyController.resetProfileSpecificSettings()
            }.onFailure { Log.e(TAG, "Failed to reset profile specific settings", it) }
            _controllerModel.value = readControllerStateSafe()
        }
    }

    companion object {
        private const val TAG = "DolbyRepository"
        private val ATTRIBUTES_MEDIA =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()

        @Volatile
        private var instance: DolbyRepository? = null

        fun getInstance(context: Context): DolbyRepository =
            instance ?: synchronized(this) {
                instance ?: DolbyRepository(context.applicationContext).also { instance = it }
            }
    }
}

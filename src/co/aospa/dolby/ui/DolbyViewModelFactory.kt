/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.aospa.dolby.data.DolbyRepository
import co.aospa.dolby.geq.data.EqualizerRepository
import co.aospa.dolby.geq.ui.EqualizerViewModel

/**
 * Application-scoped ViewModelProvider.Factory providing shared instances of
 * [DolbyRepository] and [EqualizerRepository] to both DolbyActivity and EqualizerActivity.
 */
class DolbyViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DolbyViewModel::class.java) -> {
                DolbyViewModel(DolbyRepository.getInstance(application)) as T
            }
            modelClass.isAssignableFrom(EqualizerViewModel::class.java) -> {
                EqualizerViewModel(EqualizerRepository.getInstance(application)) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var instance: DolbyViewModelFactory? = null

        fun getInstance(application: Application): DolbyViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: DolbyViewModelFactory(application).also { instance = it }
            }
    }
}

/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.geq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import co.aospa.dolby.geq.ui.EqualizerScreen
import co.aospa.dolby.geq.ui.EqualizerViewModel
import co.aospa.dolby.ui.DolbyViewModelFactory
import co.aospa.dolby.ui.theme.DolbyTheme

class EqualizerActivity : ComponentActivity() {

    private val viewModel: EqualizerViewModel by viewModels {
        DolbyViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DolbyTheme {
                EqualizerScreen(viewModel = viewModel)
            }
        }
    }
}

/*
 * Copyright (C) 2023-24 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import co.aospa.dolby.ui.DolbyViewModel
import co.aospa.dolby.ui.DolbyViewModelFactory
import co.aospa.dolby.ui.HomeScreen
import co.aospa.dolby.ui.theme.DolbyTheme

class DolbyActivity : ComponentActivity() {

    private val viewModel: DolbyViewModel by viewModels {
        DolbyViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DolbyTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}

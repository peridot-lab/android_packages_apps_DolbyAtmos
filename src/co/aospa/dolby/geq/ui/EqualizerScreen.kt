/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.geq.ui

import android.app.Activity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.aospa.dolby.R
import co.aospa.dolby.ui.theme.nestedInnerRadius
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    val outerShape = RoundedCornerShape(28.dp)
    val innerRadius = nestedInnerRadius(28.dp, 12.dp)
    val innerShape = RoundedCornerShape(innerRadius)

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(text = stringResource(R.string.dolby_preset))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (backDispatcher != null) {
                                backDispatcher.onBackPressed()
                            } else {
                                (context as? Activity)?.finish()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.dolby_back),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val insets = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
        val insetsPadding = insets.asPaddingValues()
        val bottomInset = insetsPadding.calculateBottomPadding()
        val startCutout = insetsPadding.calculateStartPadding(LocalLayoutDirection.current)
        val endCutout = insetsPadding.calculateEndPadding(LocalLayoutDirection.current)

        BoxWithConstraints(
            modifier =
                Modifier.fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.surface),
        ) {
            val isExpanded = maxWidth >= 600.dp
            val contentModifier =
                if (isExpanded) {
                    Modifier.widthIn(max = 720.dp).align(Alignment.TopCenter)
                } else {
                    Modifier.fillMaxWidth()
                }

            Column(
                modifier =
                    contentModifier
                        .verticalScroll(rememberScrollState())
                        .padding(start = startCutout, end = endCutout)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    shape = outerShape,
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        PresetSelector(viewModel = viewModel)
                    }
                }

                Card(
                    shape = outerShape,
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = innerShape,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val activePage = if (isExpanded) -1 else pagerState.currentPage
                            EqualizerCurve(
                                bandGains = uiState.currentPreset.bandGains,
                                activePage = activePage,
                                onNodeClicked = { bandIndex ->
                                    if (!isExpanded) {
                                        val targetPage = if (bandIndex < 5) 0 else 1
                                        if (pagerState.currentPage != targetPage) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(targetPage)
                                            }
                                        }
                                    }
                                },
                                onGainChanged = { bandIndex, newGain ->
                                    viewModel.setGain(bandIndex, newGain)
                                },
                            )
                        }

                        Surface(
                            shape = innerShape,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            EqualizerBands(
                                bandGains = uiState.currentPreset.bandGains,
                                pagerState = pagerState,
                                onGainChanged = { bandIndex, newGain ->
                                    viewModel.setGain(bandIndex, newGain)
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp + bottomInset))
            }
        }
    }
}

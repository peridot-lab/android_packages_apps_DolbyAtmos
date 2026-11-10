/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.geq.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import co.aospa.dolby.R
import co.aospa.dolby.ui.theme.DolbyShapes
import co.aospa.dolby.ui.theme.MorphShape
import co.aospa.dolby.ui.theme.expressiveFastSpatialSpec
import co.aospa.dolby.ui.theme.rememberDolbyHaptics

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PresetSelector(
    viewModel: EqualizerViewModel,
    modifier: Modifier = Modifier,
) {
    val presets by viewModel.presets.collectAsState()
    val currentPreset by viewModel.preset.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    var showNewPresetDialog by remember { mutableStateOf(false) }
    var showRenamePresetDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val haptics = rememberDolbyHaptics()

    val containerShape = RoundedCornerShape(16.dp)

    val saveInteractionSource = remember { MutableInteractionSource() }
    val isSavePressed by saveInteractionSource.collectIsPressedAsState()
    val saveScale by
        animateFloatAsState(
            targetValue = if (isSavePressed) 0.90f else 1.0f,
            animationSpec = expressiveFastSpatialSpec(),
            label = "SaveScale",
        )
    val saveRotation by
        animateFloatAsState(
            targetValue = if (isSavePressed) -8f else 0f,
            animationSpec = expressiveFastSpatialSpec(),
            label = "SaveRotation",
        )
    val saveMorphProgress by
        animateFloatAsState(
            targetValue = if (isSavePressed) 1f else 0f,
            animationSpec = expressiveFastSpatialSpec(),
            label = "SaveMorphProgress",
        )
    val saveShape =
        remember(saveMorphProgress) {
            MorphShape(DolbyShapes.SaveButtonMorph, saveMorphProgress)
        }

    val resetInteractionSource = remember { MutableInteractionSource() }
    val isResetPressed by resetInteractionSource.collectIsPressedAsState()
    val resetScale by
        animateFloatAsState(
            targetValue = if (isResetPressed) 0.90f else 1.0f,
            animationSpec = expressiveFastSpatialSpec(),
            label = "ResetScale",
        )
    val resetRotation by
        animateFloatAsState(
            targetValue = if (isResetPressed) 12f else 0f,
            animationSpec = expressiveFastSpatialSpec(),
            label = "ResetRotation",
        )
    val resetMorphProgress by
        animateFloatAsState(
            targetValue = if (isResetPressed) 1f else 0f,
            animationSpec = expressiveFastSpatialSpec(),
            label = "ResetMorphProgress",
        )
    val resetShape =
        remember(resetMorphProgress) {
            MorphShape(DolbyShapes.ResetButtonMorph, resetMorphProgress)
        }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f),
        ) {
            TextField(
                value = currentPreset.name,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.dolby_geq_preset)) },
                singleLine = true,
                shape = containerShape,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                modifier =
                    Modifier.menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = true,
                    )
                        .fillMaxWidth()
                        .focusProperties { canFocus = false },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                presets.forEach { preset ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color =
                                    if (preset.name == currentPreset.name) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                        },
                        onClick = {
                            haptics.selectionClick()
                            viewModel.setPreset(preset)
                            expanded = false
                        },
                    )
                }
            }
        }

        FilledIconButton(
            onClick = { showNewPresetDialog = true },
            shape = saveShape,
            interactionSource = saveInteractionSource,
            colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            modifier =
                Modifier.size(48.dp)
                    .scale(saveScale)
                    .rotate(saveRotation),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.save_as_24px),
                contentDescription = stringResource(id = R.string.dolby_geq_new_preset),
                modifier = Modifier.size(20.dp),
            )
        }

        FilledTonalIconButton(
            onClick = { showResetConfirmDialog = true },
            shape = resetShape,
            interactionSource = resetInteractionSource,
            colors =
                IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            modifier =
                Modifier.size(48.dp)
                    .scale(resetScale)
                    .rotate(resetRotation),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(id = R.string.dolby_geq_reset_gains),
                modifier = Modifier.size(20.dp),
            )
        }

        if (currentPreset.isUserDefined) {
            IconButton(
                onClick = { showRenamePresetDialog = true },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.dolby_geq_rename_preset),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.dolby_geq_delete_preset),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    if (showNewPresetDialog) {
        PresetNameDialog(
            title = stringResource(id = R.string.dolby_geq_new_preset),
            onPresetNameSet = { name ->
                haptics.confirm()
                viewModel.createNewPreset(name)
            },
            onDismissDialog = { showNewPresetDialog = false },
        )
    }

    if (showRenamePresetDialog) {
        PresetNameDialog(
            title = stringResource(id = R.string.dolby_geq_rename_preset),
            presetName = currentPreset.name,
            onPresetNameSet = { name ->
                haptics.confirm()
                viewModel.renamePreset(currentPreset, name)
            },
            onDismissDialog = { showRenamePresetDialog = false },
        )
    }

    if (showDeleteConfirmDialog) {
        ConfirmationDialog(
            text = stringResource(id = R.string.dolby_geq_delete_preset_prompt),
            onConfirm = {
                haptics.reject()
                viewModel.deletePreset(currentPreset)
                showDeleteConfirmDialog = false
            },
            onDismiss = { showDeleteConfirmDialog = false },
        )
    }

    if (showResetConfirmDialog) {
        ConfirmationDialog(
            text = stringResource(id = R.string.dolby_geq_reset_gains_prompt),
            onConfirm = {
                haptics.confirm()
                viewModel.reset()
                showResetConfirmDialog = false
            },
            onDismiss = { showResetConfirmDialog = false },
        )
    }
}

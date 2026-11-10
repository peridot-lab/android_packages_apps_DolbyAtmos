/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.geq.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.aospa.dolby.R

@Composable
fun PresetNameDialog(
    title: String,
    presetName: String = "",
    onPresetNameSet: (String) -> PresetNameValidationError?,
    onDismissDialog: () -> Unit,
) {
    var text by remember { mutableStateOf(presetName) }
    var error by remember { mutableStateOf<PresetNameValidationError?>(null) }

    AlertDialog(
        onDismissRequest = onDismissDialog,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = null
                    },
                    label = { Text(stringResource(id = R.string.dolby_geq_preset_name)) },
                    isError = error != null,
                    singleLine = true,
                )
                error?.let {
                    Text(
                        text = it.toErrorMessage(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val validationError = onPresetNameSet(text)
                    if (validationError != null) {
                        error = validationError
                    } else {
                        onDismissDialog()
                    }
                }
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissDialog) {
                Text(stringResource(id = R.string.dolby_cancel))
            }
        },
    )
}

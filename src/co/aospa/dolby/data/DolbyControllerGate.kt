/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Synchronization gate serializing DolbyController calls on [Dispatchers.Main.immediate]. */
object DolbyControllerGate {
    private val mutex = Mutex()

    suspend fun <T> execute(action: () -> T): T =
        withContext(Dispatchers.Main.immediate) {
            mutex.withLock {
                action()
            }
        }
}

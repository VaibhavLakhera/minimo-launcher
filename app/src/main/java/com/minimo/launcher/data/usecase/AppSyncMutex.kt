package com.minimo.launcher.data.usecase

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSyncMutex @Inject constructor() {
    private val mutex = Mutex()

    // App and shortcut changes can both affect favourite order, so persist them one at a time.
    suspend fun <T> withLock(action: suspend () -> T): T = mutex.withLock {
        action()
    }
}

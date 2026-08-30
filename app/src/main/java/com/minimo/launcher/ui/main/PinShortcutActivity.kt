package com.minimo.launcher.ui.main

import android.content.pm.LauncherApps
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.minimo.launcher.R
import com.minimo.launcher.data.usecase.UpdateAllShortcutsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PinShortcutActivity : ComponentActivity() {
    @Inject
    lateinit var updateAllShortcutsUseCase: UpdateAllShortcutsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val launcherApps = getSystemService(LauncherApps::class.java)
            val request = launcherApps.getPinItemRequest(intent)

            // Android owns and validates the pin request. Do not persist anything until the
            // launcher has successfully accepted it.
            if (request != null && request.isValid) {
                val shortcut = request.shortcutInfo
                val acceptSuccess = request.accept()
                if (acceptSuccess) {
                    lifecycleScope.launch {
                        try {
                            // Insert the accepted shortcut immediately so it can appear in the app
                            // drawer, then sync against Android's authoritative pinned list.
                            if (shortcut != null) {
                                updateAllShortcutsUseCase.addAcceptedShortcut(shortcut)
                            }
                            updateAllShortcutsUseCase.invoke()
                        } catch (exception: Exception) {
                            // Acceptance already succeeded at the system level. A later launcher
                            // startup sync will recover from a local persistence failure.
                            Timber.e(exception, "Unable to save accepted shortcut")
                        } finally {
                            Toast.makeText(
                                this@PinShortcutActivity,
                                getString(R.string.shortcut_pinned),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                    return
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.failed_to_pin_shortcut), Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.invalid_shortcut_request), Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling pin shortcut request")
        }

        finish()
    }
}

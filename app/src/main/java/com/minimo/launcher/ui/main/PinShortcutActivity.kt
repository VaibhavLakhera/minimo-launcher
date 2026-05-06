package com.minimo.launcher.ui.main

import android.app.Activity
import android.content.pm.LauncherApps
import android.os.Bundle
import android.widget.Toast
import com.minimo.launcher.R
import timber.log.Timber

class PinShortcutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val launcherApps = getSystemService(LauncherApps::class.java)
            val request = launcherApps.getPinItemRequest(intent)

            if (request != null && request.isValid) {
                val acceptSuccess = request.accept()
                if (acceptSuccess) {
                    Toast.makeText(this, getString(R.string.shortcut_pinned), Toast.LENGTH_SHORT)
                        .show()
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
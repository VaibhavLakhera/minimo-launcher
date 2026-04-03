package com.minimo.launcher.utils

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber

fun Activity.showAppReview() {
    try {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(this, reviewInfo)
            }
        }
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

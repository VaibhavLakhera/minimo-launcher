package com.minimo.launcher.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class ScreenTimeHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getTodayScreenTimeMillis(): Long {
        try {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                    ?: return 0L

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTimestamp = calendar.timeInMillis
            val endTimestamp = System.currentTimeMillis()

            return getScreenTimeMillis(usageStatsManager, startTimestamp, endTimestamp)
        } catch (exception: Exception) {
            Timber.e(exception)
            return 0L
        }
    }

    private fun getScreenTimeMillis(
        usageStatsManager: UsageStatsManager,
        startTimestamp: Long,
        endTimestamp: Long,
        includeSelfPackageTime: Boolean = true
    ): Long {
        val usageIntervals = mutableListOf<Pair<Long, Long>>()
        val usageEvents = usageStatsManager.queryEvents(startTimestamp, endTimestamp) ?: return 0L

        val resumedPackages = mutableMapOf<String, Pair<String, Long>>()

        var lastLauncherResumeTime: Long? = null

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            if (usageEvents.getNextEvent(event)) {
                val eventPackageName = event.packageName ?: continue

                if (includeSelfPackageTime && eventPackageName == context.packageName) {
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        lastLauncherResumeTime = event.timeStamp
                    }
                    continue
                }

                val packageClassName = event.className ?: eventPackageName

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        resumedPackages[packageClassName] = eventPackageName to event.timeStamp
                    }

                    UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                        resumedPackages[packageClassName]?.let { resumedPackage ->
                            usageIntervals.add(resumedPackage.second to event.timeStamp)
                            resumedPackages.remove(packageClassName)
                        }
                    }
                }
            }
        }

        resumedPackages.maxByOrNull {
            it.value.second
        }?.value?.let { lastResumedPackage ->
            // If the launcher was resumed AFTER this background app, then this app isn't actually in the foreground anymore.
            // So we shouldn't extend its duration to the end of the day.
            // If lastLauncherResumeTime is null or happened before the last app resume, it's safe to extend.
            val launcherTookOver =
                lastLauncherResumeTime != null && lastLauncherResumeTime > lastResumedPackage.second

            if (!launcherTookOver) {
                usageIntervals.add(lastResumedPackage.second to endTimestamp)
            }
        }

        val mergedIntervals = mergeOverlappingIntervals(usageIntervals)
        return mergedIntervals.sumOf { interval ->
            val start = max(interval.first, startTimestamp)
            val end = min(interval.second, endTimestamp)
            max(0L, end - start)
        }
    }

    private fun mergeOverlappingIntervals(intervals: List<Pair<Long, Long>>): List<Pair<Long, Long>> {
        if (intervals.isEmpty()) return emptyList()
        val sortedIntervals = intervals.sortedBy { it.first }
        val mergedIntervals = mutableListOf<Pair<Long, Long>>()
        var currentInterval = sortedIntervals[0]

        for (i in 1 until sortedIntervals.size) {
            val nextInterval = sortedIntervals[i]
            if (currentInterval.second >= nextInterval.first) {
                currentInterval = Pair(
                    currentInterval.first,
                    max(currentInterval.second, nextInterval.second)
                )
            } else {
                mergedIntervals.add(currentInterval)
                currentInterval = nextInterval
            }
        }
        mergedIntervals.add(currentInterval)
        return mergedIntervals
    }
}

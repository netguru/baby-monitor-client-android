package co.netguru.baby.monitor.client.application

import android.content.Context
import co.netguru.baby.monitor.client.application.scope.AppScope
import net.hockeyapp.android.CrashManager
import javax.inject.Inject

/**
 * Helper class that initializes a set of debugging tools
 * for the debug build type and register crash manager for release type.
 * ## Debug type tools:
 * - AndroidDevMetrics
 * - Stetho
 * - StrictMode
 * - LeakCanary
 * - Timber
 *
 * ## Release type tools:
 * - CrashManager
 */
@AppScope
class DebugMetricsHelper @Inject constructor() {

    internal fun init(context: Context) {
        CrashManager.register(context)
    }
}
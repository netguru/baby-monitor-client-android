package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.application.scope.AppScope
import timber.log.Timber
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
@Singleton
class DebugMetricsHelper @Inject constructor() {

    internal fun init() {
        Timber.plant(Timber.DebugTree())
    }
}

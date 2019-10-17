package co.netguru.baby.monitor.client.application

import android.content.Context
import android.os.Handler
import android.os.StrictMode
import co.netguru.baby.monitor.client.application.scope.AppScope
import com.facebook.stetho.Stetho
import com.frogermcs.androiddevmetrics.AndroidDevMetrics
import com.github.moduth.blockcanary.BlockCanary
import com.github.moduth.blockcanary.BlockCanaryContext
import com.nshmura.strictmodenotifier.StrictModeNotifier
import com.squareup.leakcanary.LeakCanary
import net.hockeyapp.android.CrashManager
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
@AppScope
class DebugMetricsHelper @Inject constructor() {

    internal fun init(context: Context) {
        // LeakCanary
        if (LeakCanary.isInAnalyzerProcess(context.applicationContext as App)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(context.applicationContext as App)

        //Timber
        Timber.plant(Timber.DebugTree())
    }
}

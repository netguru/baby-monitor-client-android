package co.netguru.baby.monitor.client.application

import android.preference.PreferenceManager
import co.netguru.baby.monitor.client.common.extensions.edit
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.reactivex.plugins.RxJavaPlugins
import net.majorkernelpanic.streaming.rtsp.RtspServer
import javax.inject.Inject

class App : DaggerApplication() {

    @Inject
    lateinit var debugMetricsHelper: DebugMetricsHelper

    @Inject
    lateinit var rxJavaErrorHandler: RxJavaErrorHandler

    override fun onCreate() {
        super.onCreate()
        debugMetricsHelper.init(this)
        RxJavaPlugins.setErrorHandler(rxJavaErrorHandler)
        AndroidThreeTen.init(this)

        //TODO Should be refactored!!!!
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putString(RtspServer.KEY_PORT, "$PORT")
        }
    }

    override fun applicationInjector(): AndroidInjector<App> =
        DaggerApplicationComponent.builder().create(this)

    companion object {
        internal const val PORT = 5006
    }
}

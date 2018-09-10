package co.netguru.babymonitorserver

import android.app.Application
import android.preference.PreferenceManager
import net.majorkernelpanic.streaming.rtsp.RtspServer
import timber.log.Timber

class App : Application() {

    private val PORT = "5006"

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().run {
            putString(RtspServer.KEY_PORT, PORT)
            commit()
        }
    }
}
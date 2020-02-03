package co.netguru.baby.monitor.client.application.di

import android.content.Context
import android.net.nsd.NsdManager
import co.netguru.baby.monitor.client.application.App
import co.netguru.baby.monitor.client.feature.communication.nsd.DeviceNameProvider
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient

@Module
object NetworkModule {
    @Reusable
    @Provides
    fun nsdManager(app: App): NsdManager = app.getSystemService(Context.NSD_SERVICE) as NsdManager

    @Provides
    fun nsdServiceManager(nsdManager: NsdManager, deviceNameProvider: DeviceNameProvider) =
        NsdServiceManager(nsdManager, deviceNameProvider)

    @Provides
    @Reusable
    fun provideGson() =
        Gson()

    @Provides
    @Reusable
    fun provideOkHttp() =
        OkHttpClient.Builder()
            .build()
}

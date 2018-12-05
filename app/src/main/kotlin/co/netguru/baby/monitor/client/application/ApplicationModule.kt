package co.netguru.baby.monitor.client.application

import android.content.Context
import android.net.nsd.NsdManager
import co.netguru.baby.monitor.client.application.scope.AppScope
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import co.netguru.baby.monitor.client.data.server.NsdServiceManager
import co.netguru.baby.monitor.client.feature.client.configuration.AddChildDialog
import co.netguru.baby.monitor.client.feature.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.server.player.LullabyPlayer
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
class ApplicationModule {

    @AppScope
    @Provides
    fun rxJavaErrorHandler(): RxJavaErrorHandler = RxJavaErrorHandlerImpl()

    @Reusable
    @Provides
    fun nsdManager(app: App): NsdManager = app.getSystemService(Context.NSD_SERVICE) as NsdManager

    @Provides
    fun context(app: App): Context = app.applicationContext

    @Provides
    fun nsdServiceManager(nsdManager: NsdManager) = NsdServiceManager(nsdManager)

    @Provides
    fun addChildDialog(manager: NsdServiceManager, repository: ConfigurationRepository) = AddChildDialog(manager, repository)

    @Provides
    fun lullabyPlayer(app: App) = LullabyPlayer(app)

    @Provides
    fun notificationHandler(context: Context) = NotificationHandler(context)
}

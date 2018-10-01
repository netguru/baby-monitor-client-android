package co.netguru.baby.monitor.application

import android.content.Context
import android.net.nsd.NsdManager
import co.netguru.baby.monitor.application.scope.AppScope
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
}

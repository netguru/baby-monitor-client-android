package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.application.scope.AppScope
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule {

    @AppScope
    @Provides
    fun rxJavaErrorHandler(): RxJavaErrorHandler = RxJavaErrorHandlerImpl()
}

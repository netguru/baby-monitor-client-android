package co.netguru.android.template.application

import co.netguru.android.template.application.scope.AppScope
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule {

    @AppScope
    @Provides
    fun rxJavaErrorHandler(): RxJavaErrorHandler = RxJavaErrorHandlerImpl()
}

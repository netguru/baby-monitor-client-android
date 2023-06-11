package co.netguru.baby.monitor.client.application

import android.app.Application
import co.netguru.baby.monitor.client.application.di.AppComponent
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import com.jakewharton.threetenabp.AndroidThreeTen
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

class App : Application() {

    internal lateinit var appComponent: AppComponent

    @Inject
    lateinit var debugMetricsHelper: DebugMetricsHelper

    @Inject
    lateinit var rxJavaErrorHandler: RxJavaErrorHandler

    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate() {
        super.onCreate()
        appComponent = AppComponent.create(this)
        appComponent.inject(this)
        debugMetricsHelper.init()
        RxJavaPlugins.setErrorHandler(rxJavaErrorHandler)
        AndroidThreeTen.init(this)
        firebaseRepository.initializeApp(this)
    }
}

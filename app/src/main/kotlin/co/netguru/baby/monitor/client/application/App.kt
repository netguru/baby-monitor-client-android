package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.application.di.DaggerApplicationComponent
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

class App : DaggerApplication() {

    lateinit var flutterEngine : FlutterEngine
    private var engineId:String = "flutterOnboardingEngine";

    @Inject
    lateinit var debugMetricsHelper: DebugMetricsHelper

    @Inject
    lateinit var rxJavaErrorHandler: RxJavaErrorHandler

    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate() {
        super.onCreate()

        flutterEngine = FlutterEngine(applicationContext)

        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        FlutterEngineCache
            .getInstance()
            .put(engineId, flutterEngine)

        debugMetricsHelper.init()
        RxJavaPlugins.setErrorHandler(rxJavaErrorHandler)
        AndroidThreeTen.init(this)
        firebaseRepository.initializeApp(this)
    }

    override fun applicationInjector(): AndroidInjector<App> =
            DaggerApplicationComponent.builder().create(this)
}

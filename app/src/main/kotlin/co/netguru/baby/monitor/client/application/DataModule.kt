package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.data.DataRepository
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Qualifier

@Module
class DataModule {
    private val babyNameRelay = BehaviorSubject.create<String>()

    @Provides
    @BabyName
    internal fun provideBabyNameObservable(
            dataRepository: DataRepository
    ): Observable<String> =
            babyNameRelay.apply {
                dataRepository.getFirstChild().observeForever { child ->
                    child?.name?.let(::onNext)
                }
            }

    /**
     * This [Qualifier] is to be used to pass baby name in different parts of the application, and
     * between connected devices.
     */
    @Qualifier
    annotation class BabyName
}

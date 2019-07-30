package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import dagger.Module
import dagger.Provides
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Qualifier

@Module
class DataModule {
    private val babyNameRelay = BehaviorSubject.create<String>()

    @Provides
    @BabyName
    internal fun provideBabyNameObservable(
            dataRepository: DataRepository
    ): Flowable<String> =
            dataRepository.getFirstChild().map { child ->
                child.name.orEmpty()
            }

    /**
     * This [Qualifier] is to be used to pass baby name in different parts of the application, and
     * between connected devices.
     */
    @Qualifier
    annotation class BabyName
}

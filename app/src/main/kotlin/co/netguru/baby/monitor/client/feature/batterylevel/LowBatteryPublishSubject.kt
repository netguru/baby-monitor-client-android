package co.netguru.baby.monitor.client.feature.batterylevel

import io.reactivex.subjects.PublishSubject

class LowBatteryPublishSubject (
    val publishSubject: PublishSubject<Unit> = PublishSubject.create()
)
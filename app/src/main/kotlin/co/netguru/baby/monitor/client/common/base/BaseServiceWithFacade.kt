package co.netguru.baby.monitor.client.common.base

import dagger.android.DaggerService
import javax.inject.Inject

abstract class BaseServiceWithFacade<T : ServiceFacade, C : ServiceController<T>> :
    DaggerService() {

    @Inject
    lateinit var serviceController: C

    override fun onCreate() {
        super.onCreate()
        attachServiceToController()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceController.detachService()
    }

    @Suppress("UNCHECKED_CAST")
    private fun attachServiceToController() {
        serviceController.attachService(this as T)
    }
}

interface ServiceFacade

interface ServiceController<T : ServiceFacade> {
    fun attachService(service: T)
    fun detachService()
}

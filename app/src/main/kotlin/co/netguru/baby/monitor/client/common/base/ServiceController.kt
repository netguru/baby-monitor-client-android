package co.netguru.baby.monitor.client.common.base

interface ServiceController<T : ServiceFacade> {
    fun attachService(service: T)
    fun detachService()
}
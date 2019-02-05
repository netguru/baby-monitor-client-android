package co.netguru.baby.monitor.client.data.communication

data class SingleEvent<T>(
        private val event: T,
        private var dispatched: Boolean = false
) {
    var data: T? = event
        get() = if (dispatched) {
            null
        } else {
            dispatched = true
            event
        }
        set(value) {
            dispatched = false
            field = value
        }

    fun fetchData() = event
}

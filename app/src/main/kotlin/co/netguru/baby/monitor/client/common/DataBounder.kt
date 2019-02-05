package co.netguru.baby.monitor.client.common

sealed class DataBounder<T> {

    class Complete<T> : DataBounder<T>()

    class Next<T>(val data: T) : DataBounder<T>()

    class Error<T>(val throwable: Throwable) : DataBounder<T>()
}

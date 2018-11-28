package co.netguru.baby.monitor.client.feature.common.extensions

import com.google.gson.Gson

inline fun <reified T> String.toData(): T? =
        Gson().fromJson(this@toData, T::class.java)

inline fun <reified T> T.toJson(): String =
        Gson().toJson(this@toJson)

inline fun <reified A, reified B> Pair<A?, B?>.let(action: (A, B) -> Unit) {
    if (first != null && second != null) {
        action(first!!, second!!)
    }
}

package co.netguru.baby.monitor.client.feature.common.extensions

import com.google.gson.Gson

inline fun <reified T> String.toData(): T? =
        Gson().fromJson(this@toData, T::class.java)

inline fun <reified T> T.toJson(): String =
        Gson().toJson(this@toJson)

infix fun <A, B> A?.and(that: B?): Pair<A, B>? =
        if (this == null || that == null) {
            null
        } else {
            Pair(this, that)
        }

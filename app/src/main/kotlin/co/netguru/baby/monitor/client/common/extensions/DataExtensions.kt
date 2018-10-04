package co.netguru.baby.monitor.client.common.extensions

import com.google.gson.Gson

inline fun <reified T> String.toData(): T? =
        Gson().fromJson(this@toData, T::class.java)

inline fun <reified T> T.toJson(): String =
        Gson().toJson(this@toJson)


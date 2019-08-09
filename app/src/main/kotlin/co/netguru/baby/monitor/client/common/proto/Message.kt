package co.netguru.baby.monitor.client.common.proto

import com.google.gson.annotations.SerializedName

data class Message(
        @SerializedName("baby_name")
        val babyName: String? = null
)

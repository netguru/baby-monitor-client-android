package co.netguru.baby.monitor.client.data.communication.firebase

data class FirebasePushMessage(
        val to: String,
        val title: String,
        val body: String
)

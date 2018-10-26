package co.netguru.baby.monitor.client.feature.client.home.lullabies

sealed class LullabyData {

    abstract val name: String

    data class LullabyInfo(
            override val name: String,
            val duration: String,
            val source: String = ""
    ) : LullabyData()

    data class LullabyHeader(
            override val name: String
    ) : LullabyData()
}

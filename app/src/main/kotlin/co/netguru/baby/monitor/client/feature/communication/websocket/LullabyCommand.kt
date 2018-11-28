package co.netguru.baby.monitor.client.feature.communication.websocket

data class LullabyCommand(
        val lullabyName: String,
        val action: Action
)

enum class Action {
    PLAY, RESUME, REPEAT, PAUSE, STOP
}

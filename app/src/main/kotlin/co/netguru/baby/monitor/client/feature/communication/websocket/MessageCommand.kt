package co.netguru.baby.monitor.client.feature.communication.websocket

data class MessageCommand(
        val action: MessageAction,
        val value: String?
)

enum class MessageAction {
     BABY_IS_CRYING
}


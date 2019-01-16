package co.netguru.baby.monitor.client.data.communication.websocket

enum class MessageConfirmationStatus {
    WAITING_FOR_CONFIRMATION,
    CONFIRMED,
    ERROR,
    UNDEFINED
}

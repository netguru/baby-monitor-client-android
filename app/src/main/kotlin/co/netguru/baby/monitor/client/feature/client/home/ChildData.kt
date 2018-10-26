package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.client.feature.websocket.CustomWebSocketServer

data class ChildData(
        val serverUrl: String,
        val cameraPort: Int,
        var webSocketPort: Int = CustomWebSocketServer.PORT,
        var image: String? = null,
        var name: String? = null
) {
    val rtspAddress: String
        get() = "rtsp://$serverUrl:$cameraPort"

    val webSocketAddress: String
        get() = "ws://$serverUrl:$webSocketPort"
}

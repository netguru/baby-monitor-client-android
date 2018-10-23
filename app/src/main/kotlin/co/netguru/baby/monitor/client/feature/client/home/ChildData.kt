package co.netguru.baby.monitor.client.feature.client.home

data class ChildData(
        val serverUrl: String,
        val cameraPort: Int,
        var webSocketPort: Int = 8887,
        var image: String? = null,
        var name: String? = null
) {
    val rtspAddress: String
        get() = "rtsp://$serverUrl:$cameraPort"

    val webSocketAddress: String
        get() = "ws://$serverUrl:$webSocketPort"
}

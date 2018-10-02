package co.netguru.baby.monitor.client.feature.client.home.livecamera

class LiveCameraOptions {

    internal fun provideOptions() = arrayListOf(
        "--aout=opensles",
        "--audio-time-stretch",
        "-vvv",
        "--video-filter=transform",
        "--transform-type=90"
    )

}

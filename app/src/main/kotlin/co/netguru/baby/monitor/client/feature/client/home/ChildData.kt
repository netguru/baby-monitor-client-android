package co.netguru.baby.monitor.client.feature.client.home

data class ChildData (
        val image: String,
        val name: String,
        val serverUrl: String
) {
    /**
     * needed for proper spinner data displaying
     */

    override fun toString(): String {
        return name
    }
}

package co.netguru.baby.monitor.client.feature.client.home

data class ChildSpinnerData (
        val image: String,
        val name: String
) {
    override fun toString(): String {
        return name
    }
}
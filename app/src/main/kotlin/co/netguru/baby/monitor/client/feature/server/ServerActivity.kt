package co.netguru.baby.monitor.client.feature.server

import co.netguru.baby.monitor.client.feature.common.base.FragmentHolderActivity

class ServerActivity : FragmentHolderActivity() {

    override val isWithToolbar = false

    override fun createFragmentInstance() = ServerFragment.newInstance()

    override fun getActivityTitle() = ""
}

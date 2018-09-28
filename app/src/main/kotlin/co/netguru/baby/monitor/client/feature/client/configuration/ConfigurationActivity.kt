package co.netguru.baby.monitor.client.feature.client.configuration

import co.netguru.baby.monitor.client.feature.common.base.FragmentHolderActivity

class ConfigurationActivity : FragmentHolderActivity() {

    override val isWithToolbar = false

    override fun createFragmentInstance() = ConfigurationFragment.newInstance()

    override fun getActivityTitle() = ""
}

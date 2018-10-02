package co.netguru.baby.monitor.client.feature.welcome

import co.netguru.baby.monitor.client.feature.common.base.FragmentHolderActivity

class WelcomeActivity : FragmentHolderActivity() {

    override val isWithToolbar = false

    override fun createFragmentInstance() = WelcomeFragment.newInstance()

    override fun getActivityTitle() = ""
}

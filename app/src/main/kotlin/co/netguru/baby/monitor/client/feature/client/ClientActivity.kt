package co.netguru.baby.monitor.client.feature.client

import android.content.Context
import co.netguru.baby.monitor.client.feature.common.base.FragmentHolderActivity
import org.jetbrains.anko.startActivity

class ClientActivity : FragmentHolderActivity() {

    override val isWithToolbar = false

    override fun createFragmentInstance() = ClientFragment.newInstance(
        intent.getStringExtra(SERVER_ADDRESS_KEY)
    )

    override fun getActivityTitle() = ""

    companion object {
        fun startActivity(context: Context, serverAddress: String) =
            context.startActivity<ClientActivity>(SERVER_ADDRESS_KEY to serverAddress)

        private const val SERVER_ADDRESS_KEY = "key:server_address"
    }
}

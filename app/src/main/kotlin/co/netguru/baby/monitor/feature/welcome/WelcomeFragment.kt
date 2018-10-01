package co.netguru.baby.monitor.feature.welcome

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.R
import co.netguru.baby.monitor.feature.client.configuration.ConfigurationActivity
import co.netguru.baby.monitor.feature.server.ServerActivity
import kotlinx.android.synthetic.main.fragment_welcome.*
import org.jetbrains.anko.support.v4.startActivity

//TODO Should be refactored
class WelcomeFragment : Fragment() {

    companion object {
        internal fun newInstance() = WelcomeFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_welcome, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO Should be refactored
        serverButton.setOnClickListener {
            startActivity<ServerActivity>()
            activity?.finish()
        }
        clientButton.setOnClickListener {
            startActivity<ConfigurationActivity>()
            activity?.finish()
        }
    }
}

package co.netguru.baby.monitor.client.feature.client.home.talk

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import kotlinx.android.synthetic.main.fragment_client_talk.*

class ClientTalkFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_client_talk, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clientHomeTalkVisualizerV
    }

    companion object {
        internal fun newInstance() = ClientTalkFragment()
    }
}

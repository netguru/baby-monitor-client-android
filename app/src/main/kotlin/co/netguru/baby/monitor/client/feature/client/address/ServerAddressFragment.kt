package co.netguru.baby.monitor.client.feature.client.address

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import kotlinx.android.synthetic.main.fragment_server_address.*

class ServerAddressFragment : DialogFragment() {

    companion object {
        fun newInstance() = ServerAddressFragment()

        internal const val TAG = "ServerAddressFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_server_address, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        confirmationButton.setOnClickListener {
        }
    }
}

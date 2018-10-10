package co.netguru.baby.monitor.client.feature.welcome

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import kotlinx.android.synthetic.main.fragment_welcome.*

//TODO Should be refactored
class WelcomeFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_welcome, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO Should be refactored
        serverButton.setOnClickListener {
            findNavController().navigate(R.id.actionWelcomeToServer)
        }
        clientButton.setOnClickListener {
            findNavController().navigate(R.id.actionWelcomeToConfiguration)
        }
    }
}

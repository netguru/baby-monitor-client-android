package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import kotlinx.android.synthetic.main.fragment_install_app.*

class InstallAppFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_install_app, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        installAppDoneButton.setOnClickListener {
            findNavController().navigate(R.id.actionInstallAppToConfiguration)
        }

        installAppBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}

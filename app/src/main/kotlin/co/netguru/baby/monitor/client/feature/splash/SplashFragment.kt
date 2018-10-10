package co.netguru.baby.monitor.client.feature.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SplashFragment : DaggerFragment() {

    @Inject
    lateinit var configurationRepository: ConfigurationRepository

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_splash, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (configurationRepository.childrenList.isNotEmpty()) {
            findNavController().navigate(R.id.actionSplashToClientHome)
            requireActivity().finish()
        } else {
            findNavController().navigate(R.id.actionSplashToWelcome)
        }
    }
}

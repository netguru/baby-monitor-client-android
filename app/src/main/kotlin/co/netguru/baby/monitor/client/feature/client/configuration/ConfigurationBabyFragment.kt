package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.extensions.trimmedText
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_configuring_baby.*
import javax.inject.Inject

class ConfigurationBabyFragment : DaggerFragment() {

    @Inject
    lateinit var configurationRepository: ConfigurationRepository

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_configuring_baby, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configuringBabySaveBtn.setOnClickListener {
            if (configuringBabyBabyNameMet.trimmedText.isEmpty()) return@setOnClickListener
            configurationRepository.updateChildData(
                    ChildData(
                            serverUrl = configurationRepository.childrenList.first().serverUrl,
                            name = configuringBabyBabyNameMet.trimmedText
                    )
            )
            findNavController().navigate(R.id.actionConfiguringClientHome)
            requireActivity().finish()
        }
    }
}

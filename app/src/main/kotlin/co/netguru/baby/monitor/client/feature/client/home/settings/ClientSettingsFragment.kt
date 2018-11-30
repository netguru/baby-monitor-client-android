package co.netguru.baby.monitor.client.feature.client.home.settings


import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.client.configuration.ConfigurationViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_client_settings.*
import javax.inject.Inject

class ClientSettingsFragment : DaggerFragment() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy { ViewModelProviders.of(this, factory)[ConfigurationViewModel::class.java] }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_client_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsRemoveDataBtn.setOnClickListener {
            viewModel.clearChildsData()
            requireActivity().finish()
        }
    }
}

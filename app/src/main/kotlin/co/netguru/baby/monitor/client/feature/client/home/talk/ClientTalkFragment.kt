package co.netguru.baby.monitor.client.feature.client.home.talk

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_client_talk.*
import net.majorkernelpanic.streaming.audio.AudioDataListener
import javax.inject.Inject

class ClientTalkFragment : DaggerFragment(), AudioDataListener {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.shouldHideNavbar.postValue(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_client_talk, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clientHomeTalkVisualizerV
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.shouldHideNavbar.postValue(false)
    }

    override fun onDataReady(data: ByteArray?) {
        //todo add data source
        data ?: return
        if (isResumed) {
            clientHomeTalkVisualizerV.receive(data)
        }
    }
}

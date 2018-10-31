package co.netguru.baby.monitor.client.feature.client.home.lullabies

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.view.StickyHeaderDecorator
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.websocket.Action
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_client_lullabies.*
import javax.inject.Inject

class ClientLullabiesFragment : DaggerFragment() {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val homeViewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }
    private val lullabyViewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[LullabiesViewModel::class.java]
    }
    private val lullabiesAdapter by lazy {
        LullabiesAdapter { name, action ->
            homeViewModel.manageLullabyPlayback(name, action)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_client_lullabies, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeData()
    }

    private fun setupView() {
        with(clientHomeLullabyRv) {
            adapter = lullabiesAdapter
            addItemDecoration(StickyHeaderDecorator(lullabiesAdapter))
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        lullabiesRepeatIbtn.setOnClickListener {
            homeViewModel.repeatLullaby()
        }
        lullabiesStopIbtn.setOnClickListener {
            homeViewModel.stopPlayback()
        }
        lullabiesPlayIbtn.setOnClickListener {
            homeViewModel.switchPlayback()
        }
    }

    private fun observeData() {
        lullabyViewModel.lullabiesData.observe(this, Observer { list ->
            lullabiesAdapter.lullabies = list ?: return@Observer
        })
        homeViewModel.lullabyCommand.observe(this, Observer { command ->
            command ?: return@Observer

            if (command.action == Action.PLAY || command.action == Action.RESUME) {
                lullabiesPlayIbtn.setImageResource(R.drawable.ic_pause_white_24dp)
            } else if (command.action == Action.STOP || command.action == Action.PAUSE) {
                lullabiesPlayIbtn.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            }
            lullabiesPlayingTv.text = command.lullabyName
        })
    }
}

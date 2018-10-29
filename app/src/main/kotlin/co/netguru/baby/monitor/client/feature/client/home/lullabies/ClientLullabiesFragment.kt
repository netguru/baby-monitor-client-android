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
        with(clientHomeLullabyRv) {
            adapter = lullabiesAdapter
            addItemDecoration(StickyHeaderDecorator(lullabiesAdapter))
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        observeData()
    }

    private fun observeData() {
        homeViewModel.lullabyCommands.observe(this, Observer { command ->
            lullabyViewModel.handleCommand(command ?: return@Observer)
        })
        lullabyViewModel.lullabiesData.observe(this, Observer { list ->
            lullabiesAdapter.lullabies = list ?: return@Observer
        })
    }
}

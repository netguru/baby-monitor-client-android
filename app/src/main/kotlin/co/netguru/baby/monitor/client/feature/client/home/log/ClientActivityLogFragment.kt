package co.netguru.baby.monitor.client.feature.client.home.log

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.common.view.StickyHeaderDecorator
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import kotlinx.android.synthetic.main.fragment_client_activity_log.*
import javax.inject.Inject

class ClientActivityLogFragment : BaseDaggerFragment() {
    override val layoutResource = R.layout.fragment_client_activity_log

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val logAdapter by lazy { ActivityLogAdapter() }
    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.toolbarState.postValue(ToolbarState.HISTORY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        viewModel.logData.observe(this, Observer { activities ->
            activities ?: return@Observer
            logAdapter.setupList(activities)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.toolbarState.postValue(ToolbarState.DEFAULT)
    }

    private fun setupRecyclerView() {
        with(clientActivityLogRv) {
            adapter = logAdapter
            addItemDecoration(StickyHeaderDecorator(logAdapter))
        }
    }
}

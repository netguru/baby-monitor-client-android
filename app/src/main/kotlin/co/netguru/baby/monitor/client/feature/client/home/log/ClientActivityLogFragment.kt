package co.netguru.baby.monitor.client.feature.client.home.log

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.common.view.StickyHeaderDecorator
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.feature.analytics.Screen
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import kotlinx.android.synthetic.main.fragment_client_activity_log.*
import javax.inject.Inject

class ClientActivityLogFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_client_activity_log
    override val screen: Screen = Screen.CLIENT_ACTIVITY_LOG

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val logAdapter by lazy { ActivityLogAdapter() }
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.toolbarState.postValue(ToolbarState.HISTORY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        viewModel.logData.observeNonNull(this, { activities ->
            if (activities.isNotEmpty()) {
                logAdapter.setupList(activities)
            }
            clientActivityLogRv.setVisible(activities.isNotEmpty())
            clientActivityLogEndTv.setVisible(activities.isEmpty())
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

package co.netguru.baby.monitor.client.feature.client.home.log

import android.os.Bundle
import android.view.View
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.daggerViewModel
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.common.view.StickyHeaderDecorator
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.databinding.FragmentClientActivityLogBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import javax.inject.Inject
import javax.inject.Provider

class ClientActivityLogFragment : BaseFragment(R.layout.fragment_client_activity_log) {
    override val screen: Screen = Screen.CLIENT_ACTIVITY_LOG
    private lateinit var binding : FragmentClientActivityLogBinding

    @Inject
    internal lateinit var viewModelProvider: Provider<ClientHomeViewModel>

    private val logAdapter by lazy { ActivityLogAdapter() }
    private val viewModel by daggerViewModel { viewModelProvider }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        binding = FragmentClientActivityLogBinding.inflate(layoutInflater)
        viewModel.toolbarState.postValue(ToolbarState.HISTORY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        viewModel.logData.observeNonNull(this) { activities ->
            if (activities.isNotEmpty()) {
                logAdapter.setupList(activities)
            }
            binding.clientActivityLogRv.setVisible(activities.isNotEmpty())
            binding.clientActivityLogEndTv.setVisible(activities.isEmpty())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.toolbarState.postValue(ToolbarState.DEFAULT)
    }

    private fun setupRecyclerView() {
        with(binding.clientActivityLogRv) {
            adapter = logAdapter
            addItemDecoration(StickyHeaderDecorator(logAdapter))
        }
    }
}

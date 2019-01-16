package co.netguru.baby.monitor.client.feature.client.home.log

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
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.common.extensions.getDrawableCompat
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_client_activity_log.*
import javax.inject.Inject

class ClientActivityLogFragment : DaggerFragment() {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val logAdapter by lazy { ActivityLogAdapter() }

    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_client_activity_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        viewModel.logData.observe(this, Observer { activities ->
            activities ?: return@Observer
            logAdapter.setupList(activities)
        })
    }

    private fun setupRecyclerView() {
        with(clientActivityLogRv) {
            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                val drawable = requireContext().getDrawableCompat(R.drawable.divider)
                        ?: return@apply
                setDrawable(drawable)
            }

            adapter = logAdapter
            addItemDecoration(dividerItemDecoration)
        }
    }
}

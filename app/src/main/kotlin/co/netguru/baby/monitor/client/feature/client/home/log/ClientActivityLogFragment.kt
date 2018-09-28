package co.netguru.baby.monitor.client.feature.client.home.log

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.view.StickyHeaderDecorator
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData
import kotlinx.android.synthetic.main.fragment_client_activity_log.*

class ClientActivityLogFragment : Fragment() {

    lateinit var logAdapter: ActivityLogAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_client_activity_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        with(clientActivityLogRv) {
            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            logAdapter = ActivityLogAdapter()

            adapter = logAdapter
            addItemDecoration(StickyHeaderDecorator(logAdapter))
            addItemDecoration(dividerItemDecoration)
        }
        LogActivityData.getSampleData().observe(this, Observer {
            logAdapter.setupList(it ?: return@Observer)
        })
    }
}

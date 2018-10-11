package co.netguru.baby.monitor.client.feature.client.home.lullabies

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
import co.netguru.baby.monitor.client.feature.common.DataBounder
import kotlinx.android.synthetic.main.fragment_client_lullabies.*

class ClientLullabiesFragment : Fragment() {

    private val lullabiesAdapter by lazy {
        LullabiesAdapter {
            //TODO implement logic of choosing lullaby
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_client_lullabies, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LullabyData.getSampleData().observe(this, Observer { data ->
            data ?: return@Observer
            when (data) {
                is DataBounder.Next -> {
                    with(clientHomeLullabyRv) {
                        val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
                        lullabiesAdapter.lullabies = data.data

                        clientHomeLullabyRv.adapter = lullabiesAdapter
                        addItemDecoration(StickyHeaderDecorator(lullabiesAdapter))
                        addItemDecoration(dividerItemDecoration)
                    }
                }
            }
        })
    }
}

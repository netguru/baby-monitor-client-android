package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.extensions.inTransaction
import co.netguru.baby.monitor.client.common.view.PresetedAnimations
import co.netguru.baby.monitor.client.feature.client.home.dashboard.ClientDashboardFragment
import co.netguru.baby.monitor.client.feature.client.home.log.ClientActivityLogFragment
import co.netguru.baby.monitor.client.feature.client.home.lullabies.ClientLullabiesFragment
import co.netguru.baby.monitor.client.feature.client.home.settings.ClientSettingsFragment
import co.netguru.baby.monitor.client.feature.client.home.switch_baby.ChildrenAdapter
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.layout_child_selector.*
import kotlinx.android.synthetic.main.layout_client_toolbar.*
import net.cachapa.expandablelayout.ExpandableLayout.State

class ClientHomeActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProviders.of(this)[ClientHomeActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)
        savedInstanceState ?: supportFragmentManager.inTransaction {
            add(R.id.clientHomeFrameLayout,
                    ClientDashboardFragment()
            )
        }
        setupView()
        getData()
    }

    private fun setupView() {
        clientHomeBnv.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_dashboard -> {
                    supportFragmentManager.inTransaction {
                        replace(R.id.clientHomeFrameLayout, ClientDashboardFragment())
                    }
                }

                R.id.action_activity_log -> {
                    supportFragmentManager.inTransaction {
                        replace(R.id.clientHomeFrameLayout, ClientActivityLogFragment())
                    }
                }

                R.id.action_lullabies -> {
                    supportFragmentManager.inTransaction {
                        replace(R.id.clientHomeFrameLayout, ClientLullabiesFragment())
                    }
                }

                R.id.action_settings -> {
                    supportFragmentManager.inTransaction {
                        replace(R.id.clientHomeFrameLayout, ClientSettingsFragment())
                    }
                }
            }
            true
        }
        clientHomeChildLl.setOnClickListener {
            if (clientHomeChildrenEll.isExpanded) {
                clientHomeChildrenEll.collapse()
            } else {
                clientHomeChildrenEll.expand()
            }
        }
        clientHomeChildrenEll.setOnExpansionUpdateListener { expansionFraction, state ->
            if (state == State.COLLAPSED &&
                    clientHomeChildrenCoverLl.visibility == View.VISIBLE) {

                clientHomeChildrenCoverLl.visibility = View.GONE
                clientHomeArrowIv.startAnimation(PresetedAnimations.getRotationAnimation(180f, 0f))
            } else if (state == State.EXPANDING &&
                    clientHomeChildrenCoverLl.visibility == View.GONE) {

                clientHomeChildrenCoverLl.visibility = View.VISIBLE
                clientHomeArrowIv.startAnimation(PresetedAnimations.getRotationAnimation(0f, 180f))
            }
        }
    }

    private fun getData() {
        viewModel.getChildList().observe(this, Observer { childList ->
            childList ?: return@Observer
            clientHomeChildTv.text = childList[0].name

            ChildrenAdapter(clientHomeChildrenRv).apply {
                childrenList = childList.drop(1)
                onChildSelected = { childData ->
                    clientHomeChildTv.text = childData.name
                    childrenList = childList.filter { it != childData }
                }
            }
        })
    }

    override fun onBackPressed() =
            if (clientHomeBnv.selectedItemId == R.id.action_dashboard) {
                super.onBackPressed()
            } else {
                clientHomeBnv.selectedItemId = R.id.action_dashboard
            }
}

package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.extensions.inTransaction
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.common.view.PresetedAnimations
import co.netguru.baby.monitor.client.feature.client.home.dashboard.ClientDashboardFragment
import co.netguru.baby.monitor.client.feature.client.home.log.ClientActivityLogFragment
import co.netguru.baby.monitor.client.feature.client.home.lullabies.ClientLullabiesFragment
import co.netguru.baby.monitor.client.feature.client.home.settings.ClientSettingsFragment
import co.netguru.baby.monitor.client.feature.client.home.switchbaby.ChildrenAdapter
import co.netguru.baby.monitor.client.feature.common.DataBounder
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.layout_child_selector.*
import kotlinx.android.synthetic.main.layout_client_toolbar.*
import net.cachapa.expandablelayout.ExpandableLayout.State
import javax.inject.Inject

class ClientHomeActivity : DaggerAppCompatActivity() {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ClientHomeViewModel::class.java]
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
            supportFragmentManager?.popBackStack()
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
        clientHomeChildrenEll.setOnExpansionUpdateListener(this::handleExpandableLayout)
    }

    private fun handleExpandableLayout(expansionFraction: Float, state: Int) {
        if (state == State.COLLAPSED &&
                clientHomeChildrenCoverLl.visibility == View.VISIBLE) {

            clientHomeChildrenCoverLl.setVisible(false)
            clientHomeArrowIv.startAnimation(PresetedAnimations.getRotationAnimation(180f, 0f))
        } else if (state == State.EXPANDING &&
                clientHomeChildrenCoverLl.visibility == View.GONE) {

            clientHomeChildrenCoverLl.setVisible(true)
            clientHomeArrowIv.startAnimation(PresetedAnimations.getRotationAnimation(0f, 180f))
        }
    }

    private fun getData() {
        viewModel.selectedChild.observe(this, Observer {
            it ?: return@Observer
            setSelectedChildName(it.name ?: "")
            GlideApp.with(this@ClientHomeActivity)
                    .load(it.image ?: "")
                    .apply(RequestOptions.circleCropTransform())
                    .into(clientHomeChildMiniatureIv)
        })
        viewModel.shouldHideNavbar.observe(this, Observer {
            it ?: return@Observer
            clientHomeBnv.setVisible(!it)
        })

        viewModel.getChildrenList().observe(this, Observer {
            when (it) {
                is DataBounder.Next -> {
                    val childrenAdapter: ChildrenAdapter by lazy {
                        ChildrenAdapter(
                                onChildSelected = { childData ->
                                    setSelectedChildName(childData.name ?: "")
                                    viewModel.selectedChild.postValue(childData)
                                },
                                onNewChildSelected = {
                                    //TODO implement adding new child logic here
                                }
                        ).apply {
                            originalList = it.data
                        }
                    }
                    clientHomeChildrenRv.adapter = childrenAdapter
                    clientHomeChildrenRv.setHasFixedSize(true)
                }
            }
        })
    }

    private fun setSelectedChildName(name: String) {
        clientHomeChildTv.text = if (!name.isEmpty()) {
            name
        } else {
            getString(R.string.no_name)
        }
    }

    override fun onBackPressed() =
            if (clientHomeBnv.selectedItemId == R.id.action_dashboard) {
                super.onBackPressed()
            } else {
                clientHomeBnv.selectedItemId = R.id.action_dashboard
            }
}

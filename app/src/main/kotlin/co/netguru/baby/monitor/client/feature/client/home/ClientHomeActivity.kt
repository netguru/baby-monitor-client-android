package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.extensions.getColorCompat
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.common.view.PresetedAnimations
import co.netguru.baby.monitor.client.feature.client.configuration.AddChildDialog
import co.netguru.baby.monitor.client.feature.client.home.switchbaby.ChildrenAdapter
import co.netguru.baby.monitor.client.feature.common.DataBounder
import co.netguru.baby.monitor.client.feature.websocket.ConnectionStatus
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
    @Inject
    internal lateinit var addChildDialog: AddChildDialog

    private val homeViewModel by lazy {
        ViewModelProviders.of(this, factory)[ClientHomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)
        setupView()
        getData()
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()

    override fun onBackPressed() {
        if (clientHomeChildrenEll.isExpanded) {
            clientHomeChildrenEll.collapse()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupView() {
        clientHomeBnv.setupWithNavController(
                clientDashboardNavigationHostFragment.findNavController()
        )
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
        homeViewModel.selectedChild.observe(this, Observer {
            it ?: return@Observer
            setSelectedChildName(it.name ?: "")
            GlideApp.with(this)
                    .load(it.image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(clientHomeChildMiniatureIv)
            homeViewModel.connectToServer(it, this)
        })
        homeViewModel.shouldHideNavbar.observe(this, Observer {
            it ?: return@Observer
            clientHomeBnv.setVisible(!it)
        })
        homeViewModel.selectedChildAvailability.observe(this, Observer { connection ->
            val color = when (connection) {
                ConnectionStatus.CONNECTED -> R.color.material_green_a400
                else -> R.color.material_red_a400
            }
            clientHomeChildAvailabilityIv.setBackgroundColor(getColorCompat(color))
        })

        homeViewModel.getChildrenList().observe(this, Observer {
            when (it) {
                is DataBounder.Next -> {
                    val childrenAdapter: ChildrenAdapter by lazy {
                        ChildrenAdapter(
                                onChildSelected = { childData ->
                                    setSelectedChildName(childData.name ?: "")
                                    homeViewModel.selectedChild.postValue(childData)
                                },
                                onNewChildSelected = {
                                    clientHomeChildrenEll.collapse()
                                    addChildDialog.showDialog(this,
                                            onChildAdded = {
                                                homeViewModel.setSelectedChildWithAddress(it)
                                            },
                                            onServiceConnectionError = {

                                            }
                                    )
                                }
                        ).apply {
                            selectedChild = homeViewModel.selectedChild.value ?: selectedChild
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
}

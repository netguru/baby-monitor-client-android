package co.netguru.baby.monitor.client.feature.client.home

import android.app.Service
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.feature.client.configuration.AddChildDialog
import co.netguru.baby.monitor.client.feature.client.home.switchbaby.ChildrenAdapter
import co.netguru.baby.monitor.client.feature.common.extensions.getDrawableCompat
import co.netguru.baby.monitor.client.feature.common.extensions.setVisible
import co.netguru.baby.monitor.client.feature.common.extensions.showSnackbar
import co.netguru.baby.monitor.client.feature.common.view.PresetedAnimations
import co.netguru.baby.monitor.client.feature.communication.websocket.ClientHandlerService
import co.netguru.baby.monitor.client.feature.communication.websocket.ConnectionStatus
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.layout_child_selector.*
import kotlinx.android.synthetic.main.layout_client_toolbar.*
import net.cachapa.expandablelayout.ExpandableLayout.State
import timber.log.Timber
import javax.inject.Inject

class ClientHomeActivity : DaggerAppCompatActivity(), ServiceConnection {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    @Inject
    internal lateinit var addChildDialog: AddChildDialog

    private val homeViewModel by lazy {
        ViewModelProviders.of(this, factory)[ClientHomeViewModel::class.java]
    }
    private val adapter by lazy { setupAdapter() }
    private val compositeDisposable = CompositeDisposable()
    private var childServiceBinder: ClientHandlerService.ChildServiceBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)
        setupView()
        getData()

        observeCurrentDestination()
        bindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        if (childServiceBinder != null) {
            unbindService(this)
        }
    }

    private fun observeCurrentDestination() {
        findNavController(R.id.clientDashboardNavigationHostFragment)
                .addOnNavigatedListener { controller, destination ->
                    val shouldToolbarBeVisible = (destination.id == R.id.clientLiveCamera) ||
                            homeViewModel.isBabyDataFilled()

                    clientHomeToolbarLayout.setVisible(shouldToolbarBeVisible)
                    clientToolbarCancelButton.setVisible(destination.id == R.id.clientLiveCamera)
                }
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

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("service disconnected $name")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (service is ClientHandlerService.ChildServiceBinder) {
            childServiceBinder = service
            service.getChildConnectionStatus().observe(this, Observer { childEvent ->
                val data = childEvent?.data ?: return@Observer

                val childName = if (data.first.name.isNullOrEmpty()) {
                    getString(R.string.child)
                } else {
                    data.first.name
                }
                val snackbarText = if (data.second == ConnectionStatus.DISCONNECTED) {
                    getString(R.string.client_dashboard_child_disconnected, childName)
                } else {
                    getString(R.string.client_dashboard_child_connected, childName)
                }
                window.decorView.rootView.showSnackbar(
                        snackbarText,
                        Snackbar.LENGTH_LONG)
                homeViewModel.selectedChildAvailabilityPostValue(data)
            })
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

        clientHomeToolbarAddChildAddBtn.setOnClickListener {
            showAddChildDialog()
        }

        clientToolbarCancelButton.setOnClickListener {
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()
        }
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
        })
        homeViewModel.shouldHideNavbar.observe(this, Observer {
            it ?: return@Observer
            clientHomeBnv.setVisible(!it)
        })
        homeViewModel.childList.observe(this, Observer { list ->
            list ?: return@Observer
            adapter.selectedChild = homeViewModel.selectedChild.value ?: adapter.selectedChild
            adapter.originalList = list
            clientHomeChildrenRv.adapter = adapter

            val dividerItemDecoration = DividerItemDecoration(this@ClientHomeActivity, LinearLayoutManager.VERTICAL).apply {
                val drawable = getDrawableCompat(R.drawable.divider) ?: return@apply
                setDrawable(drawable)
            }

            clientHomeChildrenRv.addItemDecoration(dividerItemDecoration)
            clientHomeChildrenRv.setHasFixedSize(true)
        })
        homeViewModel.refreshChildrenList()
    }

    private fun bindService() {
        bindService(
                Intent(this, ClientHandlerService::class.java),
                this,
                Service.BIND_AUTO_CREATE
        )
    }

    private fun setupAdapter() = ChildrenAdapter(
            onChildSelected = { childData ->
                setSelectedChildName(childData.name ?: "")
                homeViewModel.selectedChild.postValue(childData)
            },
            onNewChildSelected = {
                clientHomeChildrenEll.collapse()
                showAddChildDialog()
            }
    )

    private fun showAddChildDialog() {
        addChildDialog.showDialog(this,
                onChildAdded = { address ->
                    homeViewModel.setSelectedChildWithAddress(address)
                    homeViewModel.refreshChildrenList()
                },
                onServiceConnectionError = {}
        )
    }

    private fun setSelectedChildName(name: String) {
        clientHomeChildTv.text = if (!name.isEmpty()) {
            name
        } else {
            getString(R.string.no_name)
        }
    }
}

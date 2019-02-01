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
import android.view.Gravity
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.feature.communication.websocket.ClientHandlerService
import co.netguru.baby.monitor.client.feature.settings.DefaultDrawerObserver
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.toolbar_child.*
import kotlinx.android.synthetic.main.toolbar_default.*
import timber.log.Timber
import javax.inject.Inject

class ClientHomeActivity : DaggerAppCompatActivity(), ServiceConnection {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val homeViewModel by lazy {
        ViewModelProviders.of(this, factory)[ClientHomeViewModel::class.java]
    }
    private val compositeDisposable = CompositeDisposable()
    private var childServiceBinder: ClientHandlerService.ChildServiceBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)

        setupView()
        getData()
        bindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        if (childServiceBinder != null) {
            unbindService(this)
        }
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("service disconnected $name")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (service is ClientHandlerService.ChildServiceBinder) {
            childServiceBinder = service
            service.getChildConnectionStatus().observe(this, Observer { childEvent ->
                val data = childEvent?.data ?: return@Observer
                homeViewModel.selectedChildAvailabilityPostValue(data)
            })
        }
    }

    private fun setupView() {
        toolbarSettingsIbtn.setOnClickListener {
            homeViewModel.shouldDrawerBeOpen.postValue(true)
        }
        toolbarBackBtn.setOnClickListener {
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()
        }
    }

    private fun getData() {
        homeViewModel.fetchLogData()
        homeViewModel.selectedChild.observe(this, Observer {
            it ?: return@Observer
            setSelectedChildName(it.name ?: "")
            GlideApp.with(this)
                    .load(it.image)
                    .placeholder(R.drawable.child)
                    .apply(RequestOptions.circleCropTransform())
                    .into(toolbarChildMiniatureIv)
        })
        homeViewModel.toolbarState.observe(this, Observer(this::handleToolbarStateChange))
        homeViewModel.shouldDrawerBeOpen.observe(this, Observer { shouldClose ->
            if (shouldClose == true) {
                client_drawer.openDrawer(Gravity.END)
            } else {
                client_drawer.closeDrawer(Gravity.END)
            }
        })
        client_drawer.isDrawerOpen(Gravity.END)

        client_drawer.addDrawerListener(DefaultDrawerObserver(
                onDrawerisClosing = {
                    homeViewModel.saveChildNameRequired.postValue(true)
                }
        )
        )
    }

    private fun bindService() {
        bindService(
                Intent(this, ClientHandlerService::class.java),
                this,
                Service.BIND_AUTO_CREATE
        )
    }

    private fun setSelectedChildName(name: String) {
        toolbarChildTv.text = if (!name.isEmpty()) {
            name
        } else {
            getString(R.string.no_name)
        }
    }

    private fun handleToolbarStateChange(state: ToolbarState?) {
        when(state) {
            ToolbarState.HIDDEN -> {
                childToolbarLayout.setVisible(false)
                defaultToolbarLayout.setVisible(false)
            }
            ToolbarState.HISTORY -> {
                childToolbarLayout.setVisible(false)
                defaultToolbarLayout.setVisible(true)
                toolbarTitleTv.text = getString(R.string.latest_activity)
            }
            ToolbarState.DEFAULT -> {
                defaultToolbarLayout.setVisible(false)
                childToolbarLayout.setVisible(true)
            }
        }
    }
}

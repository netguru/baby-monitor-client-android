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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.common.extensions.showSnackbar
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.feature.communication.websocket.ClientHandlerService
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.layout_client_toolbar.*
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
        homeViewModel.saveConfiguration()

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
                    clientToolbarCancelButton.setVisible(destination.id == R.id.clientLiveCamera)
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
        settingsIbtn.setOnClickListener {
            //todo open settings drawer (21.01.2019)
        }
        clientToolbarCancelButton.setOnClickListener {
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
                    .apply(RequestOptions.circleCropTransform())
                    .into(clientHomeChildMiniatureIv)
        })
        homeViewModel.shouldHideNavbar.observe(this, Observer {
            it ?: return@Observer
            clientHomeBnv.setVisible(!it)
        })
    }

    private fun bindService() {
        bindService(
                Intent(this, ClientHandlerService::class.java),
                this,
                Service.BIND_AUTO_CREATE
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

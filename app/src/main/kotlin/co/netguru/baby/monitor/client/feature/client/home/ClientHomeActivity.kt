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
import co.netguru.baby.monitor.client.common.YesNoDialog
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.feature.babycrynotification.SnoozeNotificationUseCase.Companion.SNOOZE_DIALOG_TAG
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketClientService
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.toolbar_child.*
import kotlinx.android.synthetic.main.toolbar_default.*
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

class ClientHomeActivity : DaggerAppCompatActivity(), ServiceConnection,
    YesNoDialog.YesNoDialogClickListener {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private val homeViewModel by lazy {
        ViewModelProviders.of(this, factory)[ClientHomeViewModel::class.java]
    }
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)

        setupView()
        getData()
        bindService(
            Intent(this, WebSocketClientService::class.java),
            this,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        unbindService(this)
    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("service disconnected $name")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (service is WebSocketClientService.Binder) {
            handleWebSocketClientServiceBinder(service)
        }
    }

    private fun handleWebSocketClientServiceBinder(binder: WebSocketClientService.Binder) {
        homeViewModel.selectedChild.observeNonNull(this, { child ->
            Timber.i("Opening socket to ${child.address}.")
            homeViewModel.openSocketConnection(binder.client(), URI.create(child.address))
        })
    }

    private fun setupView() {
        toolbarSettingsIbtn.setOnClickListener {
            homeViewModel.shouldDrawerBeOpen.postValue(true)
        }
        toolbarBackBtn.setOnClickListener {
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()
        }
        homeViewModel.selectedChildAvailability.postValue(false)
    }

    private fun getData() {
        homeViewModel.fetchLogData()
        homeViewModel.selectedChild.observeNonNull(this, { child ->
            setSelectedChildName(child.name ?: "")
            GlideApp.with(this)
                .load(child.image)
                .placeholder(R.drawable.child)
                .apply(RequestOptions.circleCropTransform())
                .into(toolbarChildMiniatureIv)
        })
        homeViewModel.toolbarState.observe(this, Observer(this::handleToolbarStateChange))
        homeViewModel.backButtonState.observe(
            this,
            Observer {
                backIbtn.setVisible(it?.shouldBeVisible == true)
                setBackButtonClick(it?.shouldShowSnoozeDialog == true)
            })
        homeViewModel.shouldDrawerBeOpen.observe(this, Observer { shouldClose ->
            if (shouldClose == true) {
                client_drawer.openDrawer(Gravity.END)
            } else {
                client_drawer.closeDrawer(Gravity.END)
            }
        })
        client_drawer.isDrawerOpen(Gravity.END)
    }

    private fun setBackButtonClick(shouldShowSnoozeDialog: Boolean) {
        backIbtn.setOnClickListener {
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()
            if (shouldShowSnoozeDialog) showSnoozeDialog()
        }
    }

    private fun showSnoozeDialog() {
        YesNoDialog.newInstance(
            R.string.dialog_snooze_title,
            getString(R.string.dialog_snooze_message)
        ).show(supportFragmentManager, SNOOZE_DIALOG_TAG)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (homeViewModel.backButtonState.value?.shouldShowSnoozeDialog == true) {
            showSnoozeDialog()
        }
    }

    private fun setSelectedChildName(name: String) {
        toolbarChildTv.text = if (name.isNotEmpty()) {
            name
        } else {
            getString(R.string.no_name)
        }
    }

    private fun handleToolbarStateChange(state: ToolbarState?) {
        when (state) {
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

    override fun onYesClick(requestCode: Int, params: Bundle) {
        homeViewModel.snoozeNotifications()
    }
}

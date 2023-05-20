package co.netguru.baby.monitor.client.feature.server

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.YesNoDialog
import co.netguru.baby.monitor.client.common.extensions.controlVideoStreamVolume
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import co.netguru.baby.monitor.client.feature.onboarding.OnboardingActivity
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import co.netguru.baby.monitor.client.feature.settings.ChangeState
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_server.*
import timber.log.Timber
import javax.inject.Inject

class ServerActivity : DaggerAppCompatActivity(), ServiceConnection,
    YesNoDialog.YesNoDialogClickListener, MessageController {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private var webSocketServerServiceBinder: WebSocketServerService.Binder? = null

    private val serverViewModel by lazy {
        ViewModelProvider(this, factory)[ServerViewModel::class.java]
    }
    private val configurationViewModel by lazy {
        ViewModelProvider(this, factory)[ConfigurationViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        controlVideoStreamVolume()
        setupObservers()
        bindService(
            Intent(this, WebSocketServerService::class.java),
            this,
            Service.BIND_AUTO_CREATE
        )
    }

    private fun setupObservers() {
        configurationViewModel.resetState.observe(this, Observer { resetState ->
            when (resetState) {
                is ChangeState.Completed -> handleAppReset()
            }
        })

        serverViewModel.webSocketAction.observe(this, Observer {
            if (it == Message.RESET_ACTION) {
                configurationViewModel.resetApp()
            } else {
                Timber.d("Action not handled: $it")
            }
        })

        serverViewModel.shouldDrawerBeOpen.observe(this, Observer { shouldClose ->
            if (shouldClose) {
                server_drawer.openDrawer(GravityCompat.END)
            } else {
                server_drawer.closeDrawer(GravityCompat.END)
            }
        })
    }

    private fun handleAppReset() {
        startActivity(
            Intent(this, OnboardingActivity::class.java)
        )
        finishAffinity()
    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("Service Disconnected: $name")
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        when (service) {
            is WebSocketServerService.Binder -> {
                webSocketServerServiceBinder = service
                serverViewModel.handleWebSocketServerBinder(service)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }

    override fun onYesClick(requestCode: Int, params: Bundle) {
        webSocketServerServiceBinder?.let { serverViewModel.approvePairingCode(it) }
    }

    override fun onDismiss(requestCode: Int) {
        webSocketServerServiceBinder?.let { serverViewModel.disapprovePairingCode(it) }
    }

    override fun sendMessage(message: Message) {
        webSocketServerServiceBinder?.sendMessage(message)
    }

    override fun receivedMessages(): Observable<Message> {
        return webSocketServerServiceBinder?.messages()
            ?.flatMap {
                Observable.just(it.second)
            } ?: Observable.error(Throwable("Failed Initialisation"))
    }
}

package co.netguru.baby.monitor.client.feature.server

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.common.YesNoDialog
import co.netguru.baby.monitor.client.common.extensions.controlVideoStreamVolume
import co.netguru.baby.monitor.client.common.extensions.daggerViewModel
import co.netguru.baby.monitor.client.databinding.ActivityServerBinding
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import co.netguru.baby.monitor.client.feature.onboarding.OnboardingActivity
import co.netguru.baby.monitor.client.feature.settings.ChangeState
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class ServerActivity : AppCompatActivity(), ServiceConnection,
    YesNoDialog.YesNoDialogClickListener, MessageController {

    private var webSocketServerServiceBinder: WebSocketServerService.Binder? = null

    private val serverViewModel by daggerViewModel { serverViewModelProvider }
    private val configurationViewModel by daggerViewModel { configurationViewModelProvider }

    @Inject
    lateinit var serverViewModelProvider : Provider<ServerViewModel>

    @Inject
    lateinit var configurationViewModelProvider : Provider<ConfigurationViewModel>

    private lateinit var binding : ActivityServerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        binding = ActivityServerBinding.inflate(layoutInflater)
        controlVideoStreamVolume()
        setupObservers()
        bindService(
            Intent(this, WebSocketServerService::class.java),
            this,
            Service.BIND_AUTO_CREATE
        )
        serverViewModel.shouldDrawerBeOpen.observe(this, Observer { shouldClose ->
            with(binding) {
                if (shouldClose) {
                    serverDrawer.openDrawer(GravityCompat.END)
                } else {
                    serverDrawer.closeDrawer(GravityCompat.END)
                }
            }
        })
        setContentView(binding.root)
    }

    private fun setupObservers() {
        configurationViewModel.resetState.observe(this, Observer { resetState ->
            when (resetState) {
                is ChangeState.Completed -> handleAppReset()
                else -> {}
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
            with(binding) {
                if (shouldClose) {
                    serverDrawer.openDrawer(GravityCompat.END)
                } else {
                    serverDrawer.closeDrawer(GravityCompat.END)
                }
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

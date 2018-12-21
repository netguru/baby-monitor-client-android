package co.netguru.baby.monitor.client.feature.server

import android.Manifest.permission.*
import android.app.Service
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.DefaultServiceConnection
import co.netguru.baby.monitor.client.feature.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.feature.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.MainService
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcReceiver
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

//TODO Should be refactored
class ServerFragment : DaggerFragment(), ServiceConnection {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ServerViewModel::class.java]
    }
    private val rtcServiceIntent by lazy { Intent(requireContext(), MainService::class.java) }
    private val machineLearningIntent by lazy { Intent(requireContext(), MachineLearningService::class.java) }
    private val machineLearningServiceConnection by lazy { createDefaultServiceConnection() }
    private var rtcServiceBinder: MainService.MainBinder? = null
    private var machineLearningServiceBinder: MachineLearningService.MainBinder? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_server, container, false)

    override fun onResume() {
        super.onResume()
        viewModel.registerNsdService()
        if (!requireContext().allPermissionsGranted(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        } else {
            requireContext().bindService(
                    rtcServiceIntent,
                    this,
                    Service.BIND_AUTO_CREATE
            )
            bindMachineLearningService()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterNsdService()
        if (rtcServiceBinder != null) {
            requireContext().unbindService(this)
        }
        if (machineLearningServiceBinder != null) {
            requireContext().unbindService(machineLearningServiceConnection)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.hangUp()?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeBy(
                        onComplete = { Timber.e("disconnected") },
                        onError = Timber::e
                )
        rtcServiceBinder?.cleanup()
        machineLearningServiceBinder?.cleanup()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requireContext().allPermissionsGranted(Companion.permissions)) {
            requireContext().bindService(rtcServiceIntent, this, Service.BIND_AUTO_CREATE)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        machineLearningServiceBinder?.startRecording()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        rtcServiceBinder = service as MainService.MainBinder?
        rtcServiceBinder?.callChangeNotifier = { call ->
            machineLearningServiceBinder?.stopRecording()
            viewModel.accept(
                    call as RtcReceiver,
                    requireContext(),
                    this@ServerFragment::handleCallStateChange
            )
        }
    }

    private fun handleCallStateChange(state: CallState) {
        when (state) {
            CallState.ENDED -> {
                rtcServiceBinder?.let(this::endCall)
            }
        }
    }

    private fun endCall(binder: MainService.MainBinder) {
        binder.hangUp()
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            machineLearningServiceBinder?.startRecording()
                        },
                        onError = Timber::e
                )
    }

    private fun bindMachineLearningService() {
        requireContext().startService(machineLearningIntent)
        requireContext().bindService(
                machineLearningIntent,
                machineLearningServiceConnection,
                Service.BIND_AUTO_CREATE
        )
    }

    private fun createDefaultServiceConnection() = DefaultServiceConnection(
            onServiceConnected = { name, service ->
                machineLearningServiceBinder = service as MachineLearningService.MainBinder?
                machineLearningServiceBinder?.setOnCryingBabyDetectedListener {
                    rtcServiceBinder?.handleBabyCrying()
                }
            },
            onServiceDisconnected = {
                Timber.i("machineLearningServiceBinder disconnected")
            }
    )

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
                RECORD_AUDIO, CAMERA, WRITE_EXTERNAL_STORAGE
        )
    }
}

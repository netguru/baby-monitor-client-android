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
import co.netguru.baby.monitor.client.feature.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.feature.communication.webrtc.MainService
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcReceiver
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
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
    private var machineLearning: MachineLearning? = null
    private val serviceIntent by lazy { Intent(requireContext(), MainService::class.java) }
    private var binder: MainService.MainBinder? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_server, container, false)

    override fun onResume() {
        super.onResume()
        viewModel.registerNsdService()
        if (!requireContext().allPermissionsGranted(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        } else {
            machineLearning = MachineLearning(requireContext()).apply { init() }
            requireContext().bindService(
                    serviceIntent,
                    this,
                    Service.BIND_AUTO_CREATE
            )
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterNsdService()
        machineLearning?.dispose()
        if (binder != null) {
            requireContext().unbindService(this)
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
        binder?.cleanup()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requireContext().allPermissionsGranted(Companion.permissions)) {
            requireContext().bindService(serviceIntent, this, Service.BIND_AUTO_CREATE)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("service disconnected")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        binder = service as MainService.MainBinder?
        binder?.callChangeNotifier = { call ->
            viewModel.currentCall = call as RtcReceiver?
            viewModel.accept(requireContext())
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
                RECORD_AUDIO, CAMERA
        )
    }
}

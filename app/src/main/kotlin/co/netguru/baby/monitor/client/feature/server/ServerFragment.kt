package co.netguru.baby.monitor.client.feature.server

import android.Manifest.permission.*
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.common.extensions.toJson
import co.netguru.baby.monitor.client.feature.common.DataBounder
import co.netguru.baby.monitor.client.feature.server.player.LullabyPlayer
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_server.*
import net.majorkernelpanic.streaming.Session
import net.majorkernelpanic.streaming.audio.AudioDataListener
import net.majorkernelpanic.streaming.gl.SurfaceView
import net.majorkernelpanic.streaming.rtsp.RtspServer
import timber.log.Timber
import javax.inject.Inject

//TODO Should be refactored
class ServerFragment : DaggerFragment(), SurfaceHolder.Callback, RtspServer.CallbackListener,
        Session.Callback, AudioDataListener {
    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ServerViewModel::class.java]
    }
    private var session: Session? = null
    private var rtspServer: Intent? = null
    private val machineLearning by lazy {
        MachineLearning(requireContext(), Utils.AUDIO_SAMPLING_RATE)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_server, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        surfaceView.holder.addCallback(this)
        surfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW)

        rtspServer = Intent(requireContext(), RtspServer::class.java)
        requireActivity().startService(rtspServer)
        observeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.registerNsdService()
        if (!requireContext().allPermissionsGranted(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterNsdService()
        stopAndClearSession()
        viewModel.stopWebSocketServer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        surfaceView.holder.removeCallback(this)
        requireActivity().stopService(rtspServer)
        machineLearning.dispose()
    }

    override fun onDataReady(data: ShortArray?) {
        data ?: return
        machineLearning.feedData(data)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        createAndStartSession()
    }

    override fun onError(server: RtspServer, e: Exception, error: Int) = Timber.e(e)

    override fun onMessage(server: RtspServer, message: Int) = Unit

    override fun onBitrateUpdate(bitrate: Long) = Unit

    override fun onSessionError(reason: Int, streamType: Int, e: Exception) {
        Timber.e(e)
        stopAndClearSession()
    }

    override fun onPreviewStarted() = Unit

    override fun onSessionConfigured() = Unit

    override fun onSessionStarted() = Unit

    override fun onSessionStopped() = Unit

    override fun surfaceCreated(holder: SurfaceHolder) = createAndStartSession()

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

    private fun createAndStartSession() {
        if (requireContext().allPermissionsGranted(permissions)) {
            session = Utils.buildService(surfaceView, requireActivity(), this, this)
            session?.start()
            viewModel.setupWebSocketServer()
        }
    }

    private fun stopAndClearSession() {
        session?.stop()
        session?.release()
        session = null
    }

    private fun observeData() {
        machineLearning.result.observe(this, Observer {
            when (it) {
                is DataBounder.Next -> {
                    //TODO handle received data 18.10.2018
                    Timber.e(it.data.toJson())
                }
            }
        })
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
                RECORD_AUDIO, CAMERA, WRITE_EXTERNAL_STORAGE
        )
    }
}

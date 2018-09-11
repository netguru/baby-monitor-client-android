package co.netguru.babymonitorserver

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import android.view.WindowManager
import co.netguru.babymonitorserver.Utils.allPermissionsGranted
import co.netguru.babymonitorserver.Utils.requestPermissions
import kotlinx.android.synthetic.main.activity_main.*
import net.majorkernelpanic.streaming.Session
import net.majorkernelpanic.streaming.gl.SurfaceView
import net.majorkernelpanic.streaming.rtsp.RtspServer
import timber.log.Timber
import java.lang.Exception


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, RtspServer.CallbackListener, Session.Callback {

    private val PERMISSIONS_REQUEST_CODE = 125
    private var session: Session? = null
    private var rtspServer: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        surfaceView.holder.addCallback(this)
        surfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW)

        rtspServer = Intent(this, RtspServer::class.java)
        startService(rtspServer)
    }

    override fun onResume() {
        super.onResume()
        if (!allPermissionsGranted()) {
            requestPermissions(PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onPause() {
        super.onPause()
        stopAndClearSession()
    }

    public override fun onDestroy() {
        super.onDestroy()
        surfaceView.holder.removeCallback(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        createAndStartSession()
    }

    override fun onError(server: RtspServer, e: Exception, error: Int) {
        Timber.e(e)
    }

    override fun onMessage(server: RtspServer, message: Int) {
        Timber.e("unkown message $message")
    }

    override fun onBitrateUpdate(bitrate: Long) {

    }

    override fun onSessionError(reason: Int, streamType: Int, e: Exception) {
        Timber.w(e)
        stopAndClearSession()
    }

    override fun onPreviewStarted() {
    }

    override fun onSessionConfigured() {
        Timber.w("onSessionConfigured")
    }

    override fun onSessionStarted() {
        Timber.w("onSessionStarted")
    }

    override fun onSessionStopped() {
        Timber.w("onSessionStopped")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        createAndStartSession()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    private fun createAndStartSession() {
        if (allPermissionsGranted()) {
            session = Utils.buildService(surfaceView, this, this)
            session?.start()
        }
    }

    private fun stopAndClearSession() {
        session?.stop()
        session?.release()
        session = null
        stopService(rtspServer)
    }

}

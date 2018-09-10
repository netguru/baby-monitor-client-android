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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        surfaceView.holder.addCallback(this)
        surfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW)

        startService(Intent(this, RtspServer::class.java))
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted() && session?.isStreaming != true) {
            session = Utils.buildService(surfaceView, this, this)
            session?.start()
            session?.switchCamera()
        } else {
            requestPermissions(PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onPause() {
        super.onPause()
        session?.stop()
        session?.release()
        session = null
    }

    public override fun onDestroy() {
        super.onDestroy()
        surfaceView.holder.removeCallback(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (allPermissionsGranted()) {
            session?.start()
        }
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
        Timber.e("onSessionError")
        session?.stop()
        session?.release()
        session = null

        session = Utils.buildService(surfaceView, this, this)
        session?.start()
    }

    override fun onPreviewStarted() {
        Timber.e("onPreviewStarted")
    }

    override fun onSessionConfigured() {
        Timber.e("onSessionConfigured")
    }

    override fun onSessionStarted() {
        Timber.e("onSessionStarted")
    }

    override fun onSessionStopped() {
        Timber.e("onSessionStopped")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

}

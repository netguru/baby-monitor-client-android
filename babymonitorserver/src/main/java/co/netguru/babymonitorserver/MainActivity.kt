package co.netguru.babymonitorserver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import android.view.SurfaceHolder
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import net.majorkernelpanic.streaming.Session
import net.majorkernelpanic.streaming.SessionBuilder
import net.majorkernelpanic.streaming.audio.AudioQuality
import net.majorkernelpanic.streaming.gl.SurfaceView
import net.majorkernelpanic.streaming.rtsp.RtspServer
import timber.log.Timber
import java.lang.Exception


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, RtspServer.CallbackListener, Session.Callback {

    private val PERMISSIONS_REQUEST_CODE = 125
    private val PORT = "5006"
    private var session: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        PreferenceManager.getDefaultSharedPreferences(this).edit().run {
            putString(RtspServer.KEY_PORT, PORT)
            commit()
        }

        session = SessionBuilder.getInstance()
                .setCallback(this)
                .setSurfaceView(surfaceView)
                .setPreviewOrientation(getCameraOrientation())
                .setContext(applicationContext)
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(AudioQuality(8000, 16000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                //.setVideoQuality(new VideoQuality(800,600,20,500000))
                .build()

        surfaceView.holder.addCallback(this)
        surfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW)

        startService(Intent(this, RtspServer::class.java))
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted() && session?.isStreaming != true) {
            session?.start()
        } else {
            requestPermissions()
        }
    }

    private fun getCameraOrientation(): Int {
        val degrees = when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        var sensorOrientation = 0
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (id in manager.cameraIdList) {
            val characteristics = manager.getCameraCharacteristics(id)
            if (characteristics[CameraCharacteristics.LENS_FACING]
                    == CameraCharacteristics.LENS_FACING_FRONT) {

                val temp = (characteristics[CameraCharacteristics.SENSOR_ORIENTATION] + degrees) % 360
                sensorOrientation = (360 - temp) % 360
            }
        }
        return sensorOrientation
    }

    public override fun onDestroy() {
        super.onDestroy()
        session?.release()
        surfaceView.holder.removeCallback(this)
    }

    private fun allPermissionsGranted(): Boolean {
        var permissionsGranted = true
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = false
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = false
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = false
        }
        return permissionsGranted
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_CODE
        )
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

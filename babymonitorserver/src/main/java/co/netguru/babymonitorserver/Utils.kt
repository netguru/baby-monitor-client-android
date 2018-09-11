package co.netguru.babymonitorserver

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Size
import android.view.Surface
import net.majorkernelpanic.streaming.Session
import net.majorkernelpanic.streaming.SessionBuilder
import net.majorkernelpanic.streaming.audio.AudioQuality
import net.majorkernelpanic.streaming.gl.SurfaceView
import net.majorkernelpanic.streaming.video.VideoQuality
import timber.log.Timber

object Utils {

    private const val SAMPLING_RATE = 44100
    private const val AUDIO_BIT_RATE = 316_000

    private const val FRAME_RATE = 20
    private const val VIDEO_BIT_RATE = 300_000

    private val p720 = Size(1280, 720)

    fun buildService(
            surfaceView: SurfaceView,
            activity: Activity,
            sessionCallback: Session.Callback
    ): Session {
        val bestResolution = getBestResolution(activity)
        return SessionBuilder.getInstance()
                .setCallback(sessionCallback)
                .setSurfaceView(surfaceView)
                .setPreviewOrientation(getCameraOrientation(activity))
                .setContext(activity.applicationContext)
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(AudioQuality(SAMPLING_RATE, AUDIO_BIT_RATE))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                //.setVideoQuality(VideoQuality(bestResolution.width, bestResolution.height, FRAME_RATE, VIDEO_BIT_RATE))
                .build()
    }

    private fun getCameraOrientation(activity: Activity): Int {
        val degrees = when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        var sensorOrientation = 0
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

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

    private fun getBestResolution(context: Context): Size {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (id in manager.cameraIdList) {
            val characteristics = manager.getCameraCharacteristics(id)
            if (characteristics[CameraCharacteristics.LENS_FACING]
                    == CameraCharacteristics.LENS_FACING_FRONT) {

                val sizeList = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(SurfaceTexture::class.java)
                        .toList()
                        .also { Timber.e(it.toString()) }

                return sizeList.find { it.width <= p720.width && it.height <= p720.height } ?: continue

            }
        }
        val defaultQuality = VideoQuality.DEFAULT_VIDEO_QUALITY
        return Size(defaultQuality.resX, defaultQuality.resY)
    }

    fun Context.allPermissionsGranted(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return false
        }

        return true
    }

    fun Activity.requestPermissions(PERMISSIONS_REQUEST_CODE: Int) {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_CODE
        )
    }

}
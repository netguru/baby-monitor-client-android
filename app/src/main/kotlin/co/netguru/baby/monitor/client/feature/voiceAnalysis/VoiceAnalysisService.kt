package co.netguru.baby.monitor.client.feature.voiceAnalysis

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.widget.Toast
import androidx.annotation.UiThread
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.common.base.ServiceFacade
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class VoiceAnalysisService : Service(), ServiceFacade {

    @Inject
    lateinit var serviceController: VoiceAnalysisController

    override fun onBind(intent: Intent?) = VoiceAnalysisBinder()

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
        attachServiceToController()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceController.detachService()
    }

    private fun attachServiceToController() {
        serviceController.attachService(this)
    }

    @UiThread
    fun complain(message: String, error: Throwable) {
        Timber.w(error, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun saveDataToFile(rawData: ByteArray): Single<Boolean> {
        return WavFileGenerator.saveAudio(
            applicationContext,
            rawData,
            AacRecorder.BITS_PER_SAMPLE,
            AacRecorder.CHANNELS,
            AacRecorder.SAMPLING_RATE
        )
    }

    inner class VoiceAnalysisBinder : Binder() {
        fun startRecording() {
            Timber.i("Start recording")
            serviceController.startRecording()
        }

        fun stopRecording() {
            Timber.i("Stop recording")
            serviceController.stopRecording()
        }

        fun setVoiceAnalysisOption(voiceAnalysisOption: VoiceAnalysisOption) {
            serviceController.voiceAnalysisOption = voiceAnalysisOption
        }

        fun setNoiseDetectionLevel(level: Int) {
            serviceController.noiseLevel = level
        }
    }
}

package co.netguru.baby.monitor.client.feature.voiceAnalysis

import android.content.Intent
import android.os.Binder
import android.widget.Toast
import androidx.annotation.UiThread
import co.netguru.baby.monitor.client.common.base.BaseServiceWithFacade
import co.netguru.baby.monitor.client.common.base.ServiceFacade
import io.reactivex.Single
import timber.log.Timber

class VoiceAnalysisService : ServiceFacade,
    BaseServiceWithFacade<VoiceAnalysisService, VoiceAnalysisController>() {

    override fun onBind(intent: Intent?) = VoiceAnalysisBinder()

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

package co.netguru.baby.monitor.client.feature.voiceAnalysis

import android.content.Intent
import android.os.Binder
import android.widget.Toast
import androidx.annotation.UiThread
import co.netguru.baby.monitor.client.common.base.BaseServiceWithFacade
import co.netguru.baby.monitor.client.common.base.ServiceFacade
import timber.log.Timber

class VoiceAnalysisService : ServiceFacade,
    BaseServiceWithFacade<VoiceAnalysisService, VoiceAnalysisController>() {

    override fun onBind(intent: Intent?) = VoiceAnalysisBinder()

    @UiThread
    fun complain(message: String, error: Throwable) {
        Timber.w(error, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

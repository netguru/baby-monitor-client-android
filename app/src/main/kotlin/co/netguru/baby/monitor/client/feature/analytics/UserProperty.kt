package co.netguru.baby.monitor.client.feature.analytics

import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption

sealed class UserProperty(val value: String, val key: String) {
    class VoiceAnalysis(voiceAnalysisOption: VoiceAnalysisOption) :
        UserProperty(voiceAnalysisOption.name.toLowerCase(), VOICE_ANALYSIS_KEY)

    class AppStateProperty(appState: AppState) :
        UserProperty(appState.name.toLowerCase(), APP_STATE_KEY)

    class NoiseLevel(noiseLevel: Int) :
        UserProperty(noiseLevel.toString(), NOISE_LEVEL_KEY)

    companion object {
        private const val VOICE_ANALYSIS_KEY = "voice_analysis"
        private const val APP_STATE_KEY = "app_state"
        private const val NOISE_LEVEL_KEY = "noise_level"
    }
}

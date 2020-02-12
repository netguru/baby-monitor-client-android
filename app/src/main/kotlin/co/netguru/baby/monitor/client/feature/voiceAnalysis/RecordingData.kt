package co.netguru.baby.monitor.client.feature.voiceAnalysis

sealed class RecordingData {
    data class MachineLearning(val byteArray: ByteArray, val shortArray: ShortArray) :
        RecordingData()
    data class NoiseDetection(val shortArray: ShortArray) : RecordingData()
    data class Raw(val byteArray: ByteArray): RecordingData()
}

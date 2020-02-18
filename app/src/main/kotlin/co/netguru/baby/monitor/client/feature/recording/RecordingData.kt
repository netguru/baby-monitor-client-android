package co.netguru.baby.monitor.client.feature.recording

sealed class RecordingData {
    data class MachineLearning(val rawRecordingData: ByteArray, val shortArray: ShortArray) :
        RecordingData()

    data class NoiseDetection(val rawRecordingData: ByteArray, val shortArray: ShortArray) :
        RecordingData()

    data class Raw(val byteArray: ByteArray) : RecordingData()
}

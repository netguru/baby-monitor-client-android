package co.netguru.baby.monitor.feature.client.home.log.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Handler
import android.os.HandlerThread
import org.threeten.bp.LocalDateTime

sealed class LogActivityData {

    abstract val timeStamp: LocalDateTime

    data class LogData(
            val action: String,
            override val timeStamp: LocalDateTime
    ) : LogActivityData()

    data class LogHeader(
            override val timeStamp: LocalDateTime
    ) : LogActivityData()

    companion object {
        //TODO remove when real data is provided
        fun getSampleData(): LiveData<List<LogActivityData.LogData>> {
            val liveData = MutableLiveData<List<LogActivityData.LogData>>()
            val thread = HandlerThread("dataLoader").apply {
                start()
            }
            Handler(thread.looper).post {
                val list = mutableListOf<LogActivityData.LogData>().apply {
                    for (i in 0L..80) {
                        add(LogData("Sample action $i", LocalDateTime.now().plusHours(i)))
                    }
                }
                liveData.postValue(list)
            }
            return  liveData
        }
    }
}

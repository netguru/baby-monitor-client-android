package co.netguru.baby.monitor.client.feature.common

import android.content.Context
import co.netguru.baby.monitor.client.feature.common.extensions.subscribeWithLiveData
import io.reactivex.SingleSource
import io.reactivex.internal.operators.single.SingleDefer
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class FileManager(private val context: Context) {

    fun saveFile(cache: File?, onSuccess: (String) -> Unit) = SingleDefer.defer {
        SingleSource<Boolean> {
            if (cache == null) {
                it.onError(FileNotFoundException())
                return@SingleSource
            }
            //TODO check photo orientation
            val file = File(context.filesDir, cache.name)
            try {
                cache.copyTo(file, true)
                onSuccess.invoke(file.absolutePath)
                it.onSuccess(true)
            } catch (e: IOException) {
                e.printStackTrace()
                it.onError(e)
            }
        }
    }.subscribeOn(Schedulers.io())

    fun deleteFileIfExists(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }

}

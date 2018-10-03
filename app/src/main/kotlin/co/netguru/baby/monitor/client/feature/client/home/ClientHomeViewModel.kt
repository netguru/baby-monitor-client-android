package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
        private val configurationRepository: ConfigurationRepository
) : ViewModel() {

    internal val selectedChild = MutableLiveData<ChildData>()
    internal val shouldHideNavbar = MutableLiveData<Boolean>()

    init {
        if (configurationRepository.childrenList.isNotEmpty()) {
            selectedChild.postValue(configurationRepository.childrenList.first())
        }
    }

    fun getChildrenList(): List<ChildData> = configurationRepository.childrenList

    fun updateChildName(name: String) {
        selectedChild.value?.name = name
        configurationRepository.updateChildData(selectedChild.value)
        selectedChild.postValue(selectedChild.value)
    }

    fun updateChildImageSource(path: String) {
        selectedChild.value?.image = path
        configurationRepository.updateChildData(selectedChild.value)
        selectedChild.postValue(selectedChild.value)
    }

    fun saveImage(context: Context, imageBitmap: Bitmap) {
        HandlerThread("save_image").also {
            it.start()
            Handler(it.looper).post {
                val pattern = DateTimeFormatter.ofPattern("MMMMddyyyyHHmmss")
                val fileName = LocalDateTime.now().format(pattern) + ".jpg"
                val file = File(context.filesDir, fileName)

                try {
                    FileOutputStream(file.absoluteFile).use { out ->
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    updateChildImageSource(file.absolutePath)
                    val previousPhoto = File(selectedChild.value?.image)
                    if (previousPhoto.exists()) {
                        previousPhoto.delete()
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun saveImage(context: Context, cache: File?, onEnd: () -> Unit) {
        cache ?: return
        HandlerThread("save_image").also {
            it.start()
            Handler(it.looper).post {
                val file = File(context.filesDir, cache.name)
                try {
                    cache.copyTo(file, true)
                    val previousPhoto = File(selectedChild.value?.image)
                    if (previousPhoto.exists()) {
                        previousPhoto.delete()
                    }
                    updateChildImageSource(file.absolutePath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                onEnd.invoke()
            }
        }
    }
}

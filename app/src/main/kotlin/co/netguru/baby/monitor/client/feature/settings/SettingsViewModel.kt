package co.netguru.baby.monitor.client.feature.settings

import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class SettingsViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    fun updateChildName(name: String) {
        dataRepository.updateChildName(name)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    Timber.d("Child name updated to $name.")
                },
                onError = { error ->
                    Timber.w(error, "Couldn't update child name $name.")
                })
            .addTo(compositeDisposable)
    }

    fun saveImage(context: Context, cache: File, child: ChildDataEntity) {
        Single.fromCallable {
            val file = File(context.filesDir, cache.name)
            cache.copyTo(file, true)
            val previousPhoto = File(child.image ?: "")
            if (previousPhoto.exists()) {
                previousPhoto.delete()
            }
            file
        }.flatMapCompletable {
            dataRepository.putChildData(child.apply { image = it.path })
        }.subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    Timber.i("data updated")
                },
                onError = Timber::e
            ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun openMarket(activity: Activity) {
        val uri = Uri.parse("market://details?id=" + activity.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)

        var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        flags = if (Build.VERSION.SDK_INT >= 21) {
            flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        } else {
            flags or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
        }

        goToMarket.addFlags(flags)
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.packageName)
                )
            )
        }
    }

    fun hideKeyboard(view: View, context: Context) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }
}

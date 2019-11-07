package co.netguru.baby.monitor.client.feature.client.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Completable
import javax.inject.Inject

class RestartAppUseCase @Inject constructor() {

    fun restartApp(activity: AppCompatActivity): Completable = Completable.fromAction {
        val intent =
            activity.baseContext.packageManager.getLaunchIntentForPackage(activity.baseContext.packageName)
                ?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

        activity.startActivity(intent)
        activity.finish()
    }
}

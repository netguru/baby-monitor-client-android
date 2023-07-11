package co.netguru.baby.monitor.client.feature.firebasenotification

import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Single

class FirebaseInstanceManager {
    fun getFirebaseToken(): Single<String> = Single.create { emitter ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { emitter.onSuccess(it) }
            } else {
                emitter.onError(task.exception ?: Throwable())
            }
        }
    }

    fun invalidateFirebaseToken() {
        FirebaseMessaging.getInstance().deleteToken()
    }
}

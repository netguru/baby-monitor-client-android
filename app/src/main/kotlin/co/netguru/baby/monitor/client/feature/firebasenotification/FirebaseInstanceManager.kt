package co.netguru.baby.monitor.client.feature.firebasenotification

import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Single
import javax.inject.Inject

class FirebaseInstanceManager @Inject constructor(
    private val firebaseInstanceId: FirebaseInstanceId
) {
    fun getFirebaseToken(): Single<String> = Single.create { emitter ->
        firebaseInstanceId.instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { emitter.onSuccess(it.token) }
            } else {
                emitter.onError(task.exception ?: Throwable())
            }
        }
    }

    fun invalidateFirebaseToken() {
        firebaseInstanceId.deleteInstanceId()
    }

    companion object {
        internal const val PUSH_NOTIFICATIONS_KEY = "PUSH_NOTIFICATIONS_KEY"
    }
}

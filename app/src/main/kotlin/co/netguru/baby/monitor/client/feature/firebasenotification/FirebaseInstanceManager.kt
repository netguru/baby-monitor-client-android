package co.netguru.baby.monitor.client.feature.firebasenotification

import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Single
import javax.inject.Inject

class FirebaseInstanceManager @Inject constructor(
    private val firebaseInstanceId: FirebaseInstanceId
) {
    fun getFirebaseToken(): Single<String> = Single.create { emitter ->
        firebaseInstanceId.instanceId.addOnCompleteListener { task ->
            val result = task.result
            if (result != null) {
                emitter.onSuccess(result.token)
            } else {
                emitter.onError(task.exception ?: Throwable())
            }
        }
    }

    fun invalidateFirebaseToken() {
        firebaseInstanceId.deleteInstanceId()
    }
}

package co.netguru.baby.monitor.client.application.firebase

import android.content.Context
import android.net.Uri
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.App
import co.netguru.baby.monitor.client.feature.common.RunsInBackground
import co.netguru.baby.monitor.client.feature.machinelearning.WavFileGenerator
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class FirebaseRepository(
        private val preferencesWrapper: FirebaseSharedPreferencesWrapper,
        private val context: Context
) {
    internal var storageRef: StorageReference? = null
    internal val directory = context.getDir(WavFileGenerator.DIRECTORY_NAME, Context.MODE_PRIVATE)
    internal val compositeDisposable = CompositeDisposable()

    fun initializeApp(app: App) {
        FirebaseApp.initializeApp(app)
        continueUploadingAfterProcessRestartIfNeeded()
    }

    fun clear() {
        compositeDisposable.dispose()
    }

    internal fun uploadAllRecordingsToFirebaseStorage() {
        addListeners(uploadFirstRecording())
    }

    internal fun addListeners(uploadTask: UploadTask?) {
        uploadTask?.addOnSuccessListener { taskSnapshot ->
            removeUploadedFile(taskSnapshot)
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                            onComplete = { uploadAllRecordingsToFirebaseStorage() },
                            onError = Timber::e
                    )
                    .addTo(compositeDisposable)
        }?.addOnProgressListener { taskSnapshot ->
            preferencesWrapper.saveSessionDataIfNeeded(taskSnapshot, directory)
        }
    }

    @RunsInBackground
    internal fun continueUploadingAfterProcessRestartIfNeeded() {
        if (!preferencesWrapper.isUploadInProgress()) {
            return
        }
        if (!preferencesWrapper.isFirebaseSessionResumable()) {
            uploadAllRecordingsToFirebaseStorage()
            return
        }

        if (storageRef == null) {
            val storage = FirebaseStorage.getInstance("gs://" + context.getString(R.string.google_storage_bucket))
            storageRef = storage.reference
        }
        addListeners(storageRef?.putFile(preferencesWrapper.getFileUri(),
                StorageMetadata.Builder().build(), preferencesWrapper.getSessionUri()))
    }

    @RunsInBackground
    internal fun uploadFirstRecording(): UploadTask? {
        val file = directory.listFiles().firstOrNull()
        if (file == null) {
            preferencesWrapper.setUploadInProgress(false)
            return null
        }
        preferencesWrapper.setUploadInProgress(true)

        val storage = FirebaseStorage.getInstance("gs://" + context.getString(R.string.google_storage_bucket))
        storageRef = storage.reference
        val fileUri = Uri.fromFile(file)
        val riversRef = storageRef?.child(fileUri.lastPathSegment)
        return riversRef?.putFile(fileUri)
    }

    private fun removeUploadedFile(taskSnapshot: UploadTask.TaskSnapshot) = Completable.fromAction {
        val uploadedFile = File(directory.absolutePath + "/" + taskSnapshot.storage.path)
        uploadedFile.delete()
        storageRef = null
        preferencesWrapper.clearUploadSessionData()
    }

}

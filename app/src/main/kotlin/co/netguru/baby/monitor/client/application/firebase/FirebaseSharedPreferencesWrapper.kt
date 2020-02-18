package co.netguru.baby.monitor.client.application.firebase

import android.content.SharedPreferences
import android.net.Uri
import co.netguru.baby.monitor.client.application.di.ConfigurationPreferencesQualifier
import co.netguru.baby.monitor.client.common.extensions.edit
import com.google.firebase.storage.UploadTask
import org.threeten.bp.LocalDateTime
import java.io.File
import javax.inject.Inject

class FirebaseSharedPreferencesWrapper @Inject constructor(
        @ConfigurationPreferencesQualifier private val preferences: SharedPreferences
) {
    internal fun getSessionUriString(): String? {
        return preferences.getString(
                FIREBASE_SESSION_URI,
                null)
    }

    internal fun getFileUriString(): String? {
        return preferences.getString(
                FIREBASE_FILE_URI,
                null)
    }

    fun isSessionExpired(): Boolean {
        val uploadDateString = preferences.getString(
                FIREBASE_UPLOAD_DATE,
                LocalDateTime.now().toString())
        val weekBefore = LocalDateTime.now().minusWeeks(1)
        return LocalDateTime.parse(uploadDateString).isBefore(weekBefore)
    }

    fun isSessionOrFileUriNull(): Boolean {
        return getSessionUriString() == null ||
                getFileUriString() == null
    }

    fun isUploadEnabled() = preferences.getBoolean(
            FIREBASE_UPLOAD_ENABLED,
            false
    )

    fun getSessionUri(): Uri {
        return Uri.parse(getSessionUriString())
    }

    fun getFileUri(): Uri {
        return Uri.fromFile(File(getFileUriString()))
    }

    fun setUploadEnabled(isInProgress: Boolean) {
        preferences.edit {
            putBoolean(FIREBASE_UPLOAD_ENABLED, isInProgress)
        }
    }

    fun clearUploadSessionData() {
        preferences.edit {
            remove(FIREBASE_SESSION_URI)
            remove(FIREBASE_FILE_URI)
            remove(FIREBASE_UPLOAD_DATE)
        }
    }

    fun saveSessionDataIfNeeded(taskSnapshot: UploadTask.TaskSnapshot, filesDir: File) {
        if (getSessionUriString().isNullOrEmpty()) {
            preferences.edit {
                putString(FIREBASE_SESSION_URI, taskSnapshot.uploadSessionUri.toString())
                putString(FIREBASE_FILE_URI, filesDir.absolutePath + "/" + taskSnapshot.storage.path)
                putString(FIREBASE_UPLOAD_DATE, LocalDateTime.now().toString())
            }
        }
    }

    fun isFirebaseSessionResumable(): Boolean {
        return !isSessionExpired() && !isSessionOrFileUriNull()
    }

    companion object {
        private const val FIREBASE_UPLOAD_ENABLED = "firebase_upload_enabled"
        private const val FIREBASE_SESSION_URI = "firebase_session_uri"
        private const val FIREBASE_FILE_URI = "firebase_file_uri"
        private const val FIREBASE_UPLOAD_DATE = "firebase_upload_date"
    }
}

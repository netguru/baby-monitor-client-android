package co.netguru.baby.monitor.client.common

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun arePermissionsGranted(context: Context, vararg permission: String): Boolean {
        return !permission
            .any { !hasPermission(context, it) }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getPermissionsRequestResult(
        activity: Activity,
        requestResultCode: Int,
        resultRequestCode: Int,
        grantResults: IntArray,
        vararg permission: String
    ): PermissionResult {
        return when {
            resultRequestCode != requestResultCode -> PermissionResult.NOT_GRANTED
            grantResults.none { it == PackageManager.PERMISSION_DENIED } -> PermissionResult.GRANTED
            permission.any {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    it
                )
            } -> PermissionResult.SHOW_RATIONALE
            else -> PermissionResult.NOT_GRANTED
        }
    }
}

enum class PermissionResult {
    GRANTED,
    NOT_GRANTED,
    SHOW_RATIONALE
}

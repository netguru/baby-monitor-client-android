package co.netguru.babymonitorserver

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

fun Context.allPermissionsGranted(permissions: Array<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }

    return true
}
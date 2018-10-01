package co.netguru.baby.monitor.common.extensions

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction

fun FragmentManager.inTransaction(transaction: FragmentTransaction.() -> FragmentTransaction) {
    this.beginTransaction().transaction().commit()
}

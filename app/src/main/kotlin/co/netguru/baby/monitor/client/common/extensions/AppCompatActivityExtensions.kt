package co.netguru.baby.monitor.client.common.extensions

import android.annotation.SuppressLint
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

fun AppCompatActivity.replaceFragment(
    fragment: Fragment, @IdRes containerViewId: Int, tag: String? = null
) = getReplaceFragmentInContainerTransaction(containerViewId, fragment, tag)
    .commit()

fun AppCompatActivity.replaceWithBackStackFragmentInContainer(
    @IdRes containerViewId: Int, fragment: Fragment,
    tag: String? = null, name: String? = null
) = getReplaceFragmentInContainerTransaction(containerViewId, fragment, tag)
    .addToBackStack(name)
    .commit()

@SuppressLint("CommitTransaction")
private fun AppCompatActivity.getReplaceFragmentInContainerTransaction(
    @IdRes containerViewId: Int, fragment: Fragment, tag: String? = null
) = supportFragmentManager.beginTransaction()
    .replace(containerViewId, fragment, tag)

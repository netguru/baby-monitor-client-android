package co.netguru.baby.monitor.client.common.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup

abstract class BaseFragment: androidx.fragment.app.Fragment() {
    abstract val layoutResource: Int

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(layoutResource, container, false)
}

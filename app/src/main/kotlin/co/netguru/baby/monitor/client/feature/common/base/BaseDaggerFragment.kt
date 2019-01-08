package co.netguru.baby.monitor.client.feature.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.android.support.DaggerFragment

abstract class BaseDaggerFragment : DaggerFragment() {

    abstract val layoutResource: Int

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(layoutResource, container, false)
}

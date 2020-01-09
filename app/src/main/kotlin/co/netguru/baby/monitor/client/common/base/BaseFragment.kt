package co.netguru.baby.monitor.client.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import dagger.android.support.DaggerFragment
import javax.inject.Inject

abstract class BaseFragment : DaggerFragment(), AnalyticScreen {
    abstract val layoutResource: Int
    override val screenName: String? = null

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(layoutResource, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenName?.run {
            analyticsManager.setCurrentScreen(requireActivity(), this)
        }
    }
}

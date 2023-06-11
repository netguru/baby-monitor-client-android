package co.netguru.baby.monitor.client.common.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.Screen
import javax.inject.Inject

abstract class BaseFragment(layoutResource: Int) : Fragment(layoutResource), AnalyticScreen {
    override val screen: Screen? = null

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent.inject(this)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screen?.run {
            analyticsManager.setCurrentScreen(requireActivity(), this)
        }
    }
}

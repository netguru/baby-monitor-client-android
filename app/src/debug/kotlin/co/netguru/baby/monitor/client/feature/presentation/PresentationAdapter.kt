package co.netguru.baby.monitor.client.feature.presentation

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class PresentationAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int) = when (position) {
        0 -> PresentationAFragment()
        1 -> PresentationBFragment()
        else -> PresentationCFragment()
    }

    override fun getCount() = PAGE_COUNT

    companion object {
        internal const val PAGE_COUNT = 3
        internal const val LAST_PAGE_NUMBER = 2
        internal const val FIRST_PAGE_NUMBER = 0
    }
}

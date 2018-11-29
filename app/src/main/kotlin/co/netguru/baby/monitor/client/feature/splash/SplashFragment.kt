package co.netguru.baby.monitor.client.feature.splash

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.presentation.PresentationActivity
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashFragment : DaggerFragment() {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ClientHomeViewModel::class.java]
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_splash, container, false)

    override fun onResume() {
        super.onResume()
        viewModel.refreshChildrenList()
        viewModel.childList.observe(this, Observer(this::handleSplash))
    }

    private fun handleSplash(list: List<ChildData>?) {
        Completable
                .timer(DELAY_MILLISECONDS, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribeBy {
                    val intent = if (!list.isNullOrEmpty()) {
                        Intent(requireContext(), ClientHomeActivity::class.java)
                    } else {
                        Intent(requireContext(), PresentationActivity::class.java)
                    }
                    startActivity(intent)
                    requireActivity().finish()
                }
    }

    companion object {
        private const val DELAY_MILLISECONDS = 1000L
    }
}

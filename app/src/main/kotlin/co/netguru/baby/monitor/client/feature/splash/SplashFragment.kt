package co.netguru.baby.monitor.client.feature.splash

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import co.netguru.baby.monitor.client.feature.onboarding.OnboardingActivity
import co.netguru.baby.monitor.client.feature.server.ServerActivity
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashFragment : BaseDaggerFragment() {
    override val layoutResource = R.layout.fragment_splash

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[SplashViewModel::class.java]
    }
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSavedState()
        viewModel.appState.observe(this, Observer(this::handleSplash))
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun handleSplash(appState: AppState?) {
        Completable
                .timer(DELAY_MILLISECONDS, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribeBy {
                    val classJava: Class<*> = when (appState) {
                        AppState.SERVER -> {
                            ServerActivity::class.java
                        }
                        AppState.CLIENT -> {
                            ClientHomeActivity::class.java
                        }
                        else -> {
                            OnboardingActivity::class.java
                        }
                    }
                    startActivity(Intent(requireContext(), classJava))
                    requireActivity().finish()
                }.addTo(compositeDisposable)
    }

    companion object {
        private const val DELAY_MILLISECONDS = 1000L
    }
}

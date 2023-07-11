package co.netguru.baby.monitor.client.feature.splash

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.daggerViewModel
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.analytics.Screen
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider

class SplashFragment : BaseFragment(R.layout.fragment_splash) {
    override val screen: Screen = Screen.SPLASH

   private val viewModel by daggerViewModel { viewModelProvider }

    @Inject
    lateinit var viewModelProvider : Provider<SplashViewModel>
    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_splash, container,false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSavedState()
        viewModel.appState.observe(viewLifecycleOwner, Observer(this::handleSplash))
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun handleSplash(appState: AppState?) {
        Completable
            .timer(DELAY_MILLISECONDS, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = { handleSplashDelay(appState) }
            ).addTo(compositeDisposable)
    }

    private fun handleSplashDelay(appState: AppState?) {
        when (appState) {
            AppState.SERVER -> {
                findNavController().navigate(R.id.splashToServer)
                requireActivity().finish()
            }
            AppState.CLIENT -> {
                findNavController().navigate(R.id.splashToClientHome)
                requireActivity().finish()
            }
            AppState.UNDEFINED -> {
                findNavController().navigate(R.id.splashToInfoAboutDevices)
            }
            AppState.FIRST_OPEN -> {
                findNavController().navigate(R.id.splashToOnboarding)
            }
            null -> {}
        }
    }

    companion object {
        private const val DELAY_MILLISECONDS = 1000L
    }
}

package co.netguru.baby.monitor.client.feature.client.configuration

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_install_app.*
import timber.log.Timber
import javax.inject.Inject

class InstallAppFragment : BaseDaggerFragment() {
    override val layoutResource = R.layout.fragment_install_app

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getApplicationSavedState()
                .subscribeBy(
                        onSuccess = this::handleSavedState,
                        onError = Timber::e
                ).addTo(compositeDisposable)
        installAppDoneButton.setOnClickListener {
            findNavController().navigate(R.id.actionInstallAppToConfiguration)
        }

        installAppBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun handleSavedState(state: AppState) {
        if (state == AppState.CLIENT) {
            findNavController().navigate(R.id.actionInstallAppToConfiguration)
        }
    }
}

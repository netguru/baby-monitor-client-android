package co.netguru.baby.monitor.client.feature.client.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.application.di.GlideApp
import co.netguru.baby.monitor.client.common.YesNoDialog
import co.netguru.baby.monitor.client.common.extensions.controlVideoStreamVolume
import co.netguru.baby.monitor.client.common.extensions.daggerViewModel
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.databinding.ActivityClientHomeBinding
import co.netguru.baby.monitor.client.databinding.ToolbarChildBinding
import co.netguru.baby.monitor.client.databinding.ToolbarDefaultBinding
import co.netguru.baby.monitor.client.feature.babynotification.SnoozeNotificationUseCase.Companion.SNOOZE_DIALOG_TAG
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.onboarding.OnboardingActivity
import co.netguru.baby.monitor.client.feature.settings.ChangeState
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import java.net.URI
import javax.inject.Inject
import javax.inject.Provider

class ClientHomeActivity : DaggerAppCompatActivity(),
    YesNoDialog.YesNoDialogClickListener {

    private val homeViewModel by daggerViewModel { homeViewModelProvider }

    private val configurationViewModel by daggerViewModel { configurationViewModelProvider }

    @Inject
    lateinit var configurationViewModelProvider: Provider<ConfigurationViewModel>

    @Inject
    lateinit var homeViewModelProvider: Provider<ClientHomeViewModel>

    private lateinit var binding: ActivityClientHomeBinding
    private lateinit var toolbarChildBinding: ToolbarChildBinding
    private lateinit var toolbarDefaultBinding: ToolbarDefaultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)
        appComponent.inject(this)
        controlVideoStreamVolume()
        binding = ActivityClientHomeBinding.inflate(layoutInflater)
        toolbarChildBinding = ToolbarChildBinding.bind(binding.clientDrawer)
        toolbarDefaultBinding = ToolbarDefaultBinding.bind(binding.clientDrawer)

        setupView()
        setupObservers()

        homeViewModel.fetchLogData()
        homeViewModel.checkInternetConnection()
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.openSocketConnection { address -> URI.create(address) }
    }

    override fun onPause() {
        super.onPause()
        homeViewModel.closeSocketConnection()
    }

    private fun setupObservers() {
        homeViewModel.internetConnectionAvailability.observe(this, Observer { isConnected ->
            if (!isConnected) showErrorSnackbar(R.string.no_internet_message)
        })

        homeViewModel.selectedChildLiveData.observeNonNull(this) { child ->
            handleSelectedChild(child)
        }
        homeViewModel.toolbarState.observe(this, Observer(this::handleToolbarStateChange))

        homeViewModel.backButtonState.observe(
            this,
            Observer {
                toolbarChildBinding.backIbtn.setVisible(it?.shouldBeVisible == true)
                setBackButtonClick(it?.shouldShowSnoozeDialog == true)
            })

        homeViewModel.shouldDrawerBeOpen.observe(this, Observer { shouldClose ->
            handleDrawerEvent(shouldClose)
        })

        homeViewModel.webSocketAction.observe(this, Observer {
            if (it == Message.RESET_ACTION) {
                configurationViewModel.resetApp()
            } else {
                Timber.d("Action not handled: $it")
            }
        })
        homeViewModel.errorAction.observe(this, Observer {
            showErrorSnackbar(R.string.default_error_message)
        })

        configurationViewModel.resetState.observe(this, Observer { resetState ->
            when (resetState) {
                is ChangeState.Completed -> handleAppReset()
                else -> {}
            }
        })
    }

    private fun handleAppReset() {
        startActivity(
            Intent(this, OnboardingActivity::class.java)
        )
        finishAffinity()
    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()

    private fun setupView() {
        toolbarChildBinding.toolbarSettingsIbtn.setOnClickListener {
            homeViewModel.shouldDrawerBeOpen.postValue(true)
        }
        toolbarDefaultBinding.toolbarBackBtn.setOnClickListener {
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()
        }
        binding.clientDrawer.isDrawerOpen(GravityCompat.END)
    }

    private fun handleSelectedChild(child: ChildDataEntity) {
        setSelectedChildName(child.name ?: "")
        GlideApp.with(this)
            .load(child.image)
            .placeholder(R.drawable.baby_logo)
            .apply(RequestOptions.circleCropTransform())
            .into(toolbarChildBinding.toolbarChildMiniatureIv)
    }

    private fun handleDrawerEvent(shouldClose: Boolean?) {
        with(binding) {
            if (shouldClose == true) {
                clientDrawer.openDrawer(GravityCompat.END)
            } else {
                clientDrawer.closeDrawer(GravityCompat.END)
            }
        }
    }

    private fun showErrorSnackbar(messageResource: Int) {
        Snackbar.make(binding.coordinator, messageResource, Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.restart)) {
                homeViewModel.restartApp(this)
            }
            .show()
    }

    private fun setBackButtonClick(shouldShowSnoozeDialog: Boolean) {
        toolbarChildBinding.backIbtn.setOnClickListener {
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()
            if (shouldShowSnoozeDialog) showSnoozeDialog()
        }
    }

    private fun showSnoozeDialog() {
        YesNoDialog.newInstance(
            R.string.dialog_snooze_title,
            getString(R.string.dialog_snooze_message)
        ).show(supportFragmentManager, SNOOZE_DIALOG_TAG)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (homeViewModel.backButtonState.value?.shouldShowSnoozeDialog == true) {
            showSnoozeDialog()
        }
    }

    private fun setSelectedChildName(name: String) {
        toolbarChildBinding.toolbarChildTv.text = if (name.isNotEmpty()) {
            name
        } else {
            getString(R.string.no_name)
        }
    }

    private fun handleToolbarStateChange(state: ToolbarState?) {
        with(binding) {
            when (state) {
                ToolbarState.HIDDEN -> {
                    childToolbarLayout.root.visibility = View.INVISIBLE
                    defaultToolbarLayout.root.visibility = View.INVISIBLE
                }

                ToolbarState.HISTORY -> {
                    binding.apply {
                        childToolbarLayout.root.visibility = View.INVISIBLE
                        defaultToolbarLayout.root.visibility = View.VISIBLE
                        binding.defaultToolbarLayout.toolbarTitleTv.text = getString(R.string.latest_activity)
                    }
                }

                ToolbarState.DEFAULT -> {
                    defaultToolbarLayout.root.visibility = View.INVISIBLE
                    childToolbarLayout.root.visibility = View.VISIBLE
                }

                else -> {}
            }
        }
    }

    override fun onYesClick(requestCode: Int, params: Bundle) {
        homeViewModel.snoozeNotifications()
    }
}

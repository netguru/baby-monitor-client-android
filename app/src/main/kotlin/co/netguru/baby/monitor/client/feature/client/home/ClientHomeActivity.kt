package co.netguru.baby.monitor.client.feature.client.home

import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.YesNoDialog
import co.netguru.baby.monitor.client.common.extensions.controlVideoStreamVolume
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.feature.babycrynotification.SnoozeNotificationUseCase.Companion.SNOOZE_DIALOG_TAG
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.onboarding.OnboardingActivity
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import co.netguru.baby.monitor.client.feature.settings.ChangeState
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.toolbar_child.*
import kotlinx.android.synthetic.main.toolbar_default.*
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

class ClientHomeActivity : DaggerAppCompatActivity(),
    YesNoDialog.YesNoDialogClickListener {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private val homeViewModel by lazy {
        ViewModelProviders.of(this, factory)[ClientHomeViewModel::class.java]
    }

    private val configurationViewModel by lazy {
        ViewModelProviders.of(this, factory)[ConfigurationViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)
        controlVideoStreamVolume()

        setupView()
        setupObservers()

        homeViewModel.fetchLogData()
        homeViewModel.checkInternetConnection()
        homeViewModel.openSocketConnection { address -> URI.create(address) }
    }

    private fun setupObservers() {
        homeViewModel.internetConnectionAvailability.observe(this, Observer { isConnected ->
            if (!isConnected) showNoInternetSnackbar()
        })

        homeViewModel.selectedChildLiveData.observeNonNull(this) { child ->
            handleSelectedChild(child)
        }
        homeViewModel.toolbarState.observe(this, Observer(this::handleToolbarStateChange))

        homeViewModel.backButtonState.observe(
            this,
            Observer {
                backIbtn.setVisible(it?.shouldBeVisible == true)
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

        configurationViewModel.resetState.observe(this, Observer { resetState ->
            when (resetState) {
                is ChangeState.Completed -> handleAppReset()
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
        toolbarSettingsIbtn.setOnClickListener {
            homeViewModel.shouldDrawerBeOpen.postValue(true)
        }
        toolbarBackBtn.setOnClickListener {
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()
        }
        client_drawer.isDrawerOpen(GravityCompat.END)
    }

    private fun handleSelectedChild(child: ChildDataEntity) {
        setSelectedChildName(child.name ?: "")
        GlideApp.with(this)
            .load(child.image)
            .placeholder(R.drawable.baby_logo)
            .apply(RequestOptions.circleCropTransform())
            .into(toolbarChildMiniatureIv)
    }

    private fun handleDrawerEvent(shouldClose: Boolean?) {
        if (shouldClose == true) {
            client_drawer.openDrawer(GravityCompat.END)
        } else {
            client_drawer.closeDrawer(GravityCompat.END)
        }
    }

    private fun showNoInternetSnackbar() {
        Snackbar.make(coordinator, R.string.no_internet_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.restart)) {
                homeViewModel.restartApp(this)
            }
            .show()
    }

    private fun setBackButtonClick(shouldShowSnoozeDialog: Boolean) {
        backIbtn.setOnClickListener {
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
        toolbarChildTv.text = if (name.isNotEmpty()) {
            name
        } else {
            getString(R.string.no_name)
        }
    }

    private fun handleToolbarStateChange(state: ToolbarState?) {
        when (state) {
            ToolbarState.HIDDEN -> {
                childToolbarLayout.setVisible(false)
                defaultToolbarLayout.setVisible(false)
            }
            ToolbarState.HISTORY -> {
                childToolbarLayout.setVisible(false)
                defaultToolbarLayout.setVisible(true)
                toolbarTitleTv.text = getString(R.string.latest_activity)
            }
            ToolbarState.DEFAULT -> {
                defaultToolbarLayout.setVisible(false)
                childToolbarLayout.setVisible(true)
            }
        }
    }

    override fun onYesClick(requestCode: Int, params: Bundle) {
        homeViewModel.snoozeNotifications()
    }
}

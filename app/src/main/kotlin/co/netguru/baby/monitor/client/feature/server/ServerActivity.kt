package co.netguru.baby.monitor.client.feature.server

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.Gravity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_server.*
import javax.inject.Inject

class ServerActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy { ViewModelProviders.of(this, factory)[ServerViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        observeCloseButtonFromDrawer()
    }

    private fun observeCloseButtonFromDrawer() {
        viewModel.shouldDrawerBeOpen.observe(this, Observer { shouldClose ->
            shouldClose ?: return@Observer
            if (shouldClose) {
                server_drawer.openDrawer(GravityCompat.END)
            } else {
                server_drawer.closeDrawer(GravityCompat.END)
            }
        })
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.clientDashboardNavigationHostFragment).navigateUp()
}

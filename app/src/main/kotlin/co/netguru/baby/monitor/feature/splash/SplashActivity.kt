package co.netguru.baby.monitor.feature.splash

import android.os.Bundle
import co.netguru.baby.monitor.data.server.ConfigurationRepository
import co.netguru.baby.monitor.feature.client.home.ClientHomeActivity
import co.netguru.baby.monitor.feature.welcome.WelcomeActivity
import dagger.android.support.DaggerAppCompatActivity
import org.jetbrains.anko.startActivity
import javax.inject.Inject

class SplashActivity : DaggerAppCompatActivity() {

    @Inject
    internal lateinit var configurationRepository: ConfigurationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!configurationRepository.serverAddress.isEmpty()) {
            startActivity<ClientHomeActivity>()
        } else {
            startActivity<WelcomeActivity>()
        }
        finish()
    }
}

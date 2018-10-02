package co.netguru.baby.monitor.client.feature.splash

import android.os.Bundle
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import co.netguru.baby.monitor.client.feature.welcome.WelcomeActivity
import dagger.android.support.DaggerAppCompatActivity
import org.jetbrains.anko.startActivity
import javax.inject.Inject

class SplashActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var configurationRepository: ConfigurationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (configurationRepository.serverAddress.isEmpty()) {
            startActivity<WelcomeActivity>()
        } else {
            startActivity<ClientHomeActivity>()
        }
        finish()
    }
}

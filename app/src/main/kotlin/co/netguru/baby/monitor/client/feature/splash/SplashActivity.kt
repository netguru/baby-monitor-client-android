package co.netguru.baby.monitor.client.feature.splash

import android.os.Bundle
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import co.netguru.baby.monitor.client.feature.welcome.WelcomeActivity
import dagger.android.support.DaggerAppCompatActivity
import org.jetbrains.anko.startActivity
import timber.log.Timber
import javax.inject.Inject

class SplashActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var configurationRepository: ConfigurationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (configurationRepository.childrenList.isNotEmpty()) {
            startActivity<ClientHomeActivity>()
        } else {
            startActivity<WelcomeActivity>()
        }
        finish()
    }
}

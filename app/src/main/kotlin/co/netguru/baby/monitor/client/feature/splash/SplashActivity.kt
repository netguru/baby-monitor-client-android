package co.netguru.baby.monitor.client.feature.splash

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import co.netguru.baby.monitor.client.feature.welcome.WelcomeActivity
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity<WelcomeActivity>()
        finish()
    }
}

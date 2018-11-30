package co.netguru.baby.monitor.client.feature.splash

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.extensions.inTransaction

class EnterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportFragmentManager.inTransaction {
            add(R.id.splashFl, SplashFragment(), null)
        }
    }
}

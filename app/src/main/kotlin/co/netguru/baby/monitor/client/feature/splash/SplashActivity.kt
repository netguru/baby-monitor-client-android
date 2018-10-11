package co.netguru.baby.monitor.client.feature.splash

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.splashNavigationHostFragment).navigateUp()
}

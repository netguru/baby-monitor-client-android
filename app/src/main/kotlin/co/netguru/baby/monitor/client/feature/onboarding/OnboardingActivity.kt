package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.server.ServerFragment

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.onboardingNavigationHostFragment).navigateUp()

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.onboardingNavigationHostFragment)
        if (navHostFragment?.childFragmentManager?.fragments?.get(0) is ServerFragment) {
            findNavController(R.id.onboardingNavigationHostFragment).navigate(R.id.onboardingServerToWelcome)
        } else {
            super.onBackPressed()
        }
    }
}

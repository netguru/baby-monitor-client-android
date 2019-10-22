package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
    }

    override fun onBackPressed() {
        val controller = findNavController(R.id.onboardingNavigationHostFragment).currentDestination?.id
                ?: 0
        if (controller == R.id.permissionMicrophone
                || controller == R.id.permissionMicrophone
                || controller == R.id.setupInformation) {
            findNavController(R.id.onboardingNavigationHostFragment).popBackStack(R.id.connectWiFi, false)
        } else if (controller == R.id.allDone) {
            findNavController(R.id.onboardingNavigationHostFragment).popBackStack(R.id.secondAppInfo, false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.onboardingNavigationHostFragment).navigateUp()
}

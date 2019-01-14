package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import androidx.navigation.findNavController
import co.netguru.baby.monitor.client.R

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.onboardingNavigationHostFragment).navigateUp()
}

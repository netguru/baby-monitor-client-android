package co.netguru.baby.monitor.client.feature.presentation

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.extensions.setVisible
import co.netguru.baby.monitor.client.feature.onboarding.OnboardingActivity
import kotlinx.android.synthetic.main.activity_presentation.*

class PresentationActivity : FragmentActivity() {

    private val adapter by lazy { PresentationAdapter(supportFragmentManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation)
        setupView()
    }

    private fun setupView() {
        presentationVp.adapter = adapter
        presentationBpi.setViewPager(presentationVp)
        presentationNextBtn.setOnClickListener {
            presentationVp.currentItem += 1
            changeButtonVisibility()
        }
        presentationConfigurationBtn.setOnClickListener {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }

    private fun changeButtonVisibility() {
        if (presentationVp.currentItem == LAST_PAGE_NUMBER) {
            presentationNextBtn.setVisible(false)
            presentationConfigurationBtn.setVisible(true)
        } else {
            presentationNextBtn.setVisible(true)
            presentationConfigurationBtn.setVisible(false)
        }
    }

    override fun onBackPressed() {
        changeButtonVisibility()
        if (presentationVp.currentItem != FIRST_PAGE_NUMBER) {
            presentationVp.currentItem -= 1
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val LAST_PAGE_NUMBER = 2
        private const val FIRST_PAGE_NUMBER = 0
    }
}

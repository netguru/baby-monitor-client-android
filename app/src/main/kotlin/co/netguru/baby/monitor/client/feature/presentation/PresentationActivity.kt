package co.netguru.baby.monitor.client.feature.presentation

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import co.netguru.baby.monitor.client.BuildConfig
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
        presentationVp.setOnTouchListener { v, event -> true }
        presentationBpi.setViewPager(presentationVp)
        presentationNextBtn.setOnClickListener {
            presentationVp.currentItem += 1
            changeButtonVisibility()
        }
        presentationConfigurationBtn.setOnClickListener {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
        presentationBpi.setOnSurfaceCount(PresentationAdapter.PAGE_COUNT)
    }

    private fun changeButtonVisibility() {
        if (presentationVp.currentItem == PresentationAdapter.LAST_PAGE_NUMBER) {
            presentationNextBtn.setVisible(false)
            presentationConfigurationBtn.setVisible(true)
        } else {
            presentationNextBtn.setVisible(true)
            presentationConfigurationBtn.setVisible(false)
        }
    }

    override fun onBackPressed() {
        if (presentationVp.currentItem != PresentationAdapter.FIRST_PAGE_NUMBER) {
            presentationVp.currentItem -= 1
            changeButtonVisibility()
        } else {
            super.onBackPressed()
        }
    }
}

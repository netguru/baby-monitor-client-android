package co.netguru.baby.monitor.client.common.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.extensions.inTransaction
import kotlinx.android.synthetic.main.toolbar.*

abstract class FragmentHolderActivity : AppCompatActivity() {

    open val isWithToolbar: Boolean = true

    open val shouldDisplayHomeAsUpEnabled = true

    abstract fun createFragmentInstance(): Fragment

    abstract fun getActivityTitle(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        if (isWithToolbar) {
            setupActionBar()
        }
        savedInstanceState ?: supportFragmentManager.inTransaction {
            replace(R.id.fragmentContainer, createFragmentInstance(), null)
        }
    }

    private fun getLayoutId() = if (isWithToolbar) {
        R.layout.activity_details_with_toolbar
    } else {
        R.layout.activity_details
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(shouldDisplayHomeAsUpEnabled)
            title = getActivityTitle()
        }
    }
}

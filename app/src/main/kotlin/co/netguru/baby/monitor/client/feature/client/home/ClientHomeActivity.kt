package co.netguru.baby.monitor.client.feature.client.home

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.extensions.inTransaction
import co.netguru.baby.monitor.client.feature.client.home.log.ClientActivityLogFragment
import co.netguru.baby.monitor.client.feature.client.home.dashboard.ClientDashboardFragment
import co.netguru.baby.monitor.client.feature.client.home.lullabies.ClientLullabiesFragment
import co.netguru.baby.monitor.client.feature.client.home.settings.ClientSettingsFragment
import kotlinx.android.synthetic.main.activity_client_home.*
import org.jetbrains.anko.startActivity

class ClientHomeActivity : AppCompatActivity() {

    companion object {
        internal fun startActivity(context: Context) =
            context.startActivity<ClientHomeActivity>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)
        savedInstanceState ?: supportFragmentManager.inTransaction {
            add(R.id.clientHomeFrameLayout,
                ClientDashboardFragment()
            )
        }
        setupView()
    }

    private fun setupView() = clientHomeBnv.setOnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.action_dashboard -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.clientHomeFrameLayout,
                        ClientDashboardFragment()
                    )
                }
            }

            R.id.action_activity_log -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.clientHomeFrameLayout,
                        ClientActivityLogFragment()
                    )
                }
            }

            R.id.action_lullabies -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.clientHomeFrameLayout,
                        ClientLullabiesFragment()
                    )
                }
            }

            R.id.action_settings -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.clientHomeFrameLayout,
                        ClientSettingsFragment()
                    )
                }
            }
        }
        true
    }

    override fun onBackPressed() =
        if (clientHomeBnv.selectedItemId == R.id.action_dashboard) {
            super.onBackPressed()
        } else {
            clientHomeBnv.selectedItemId = R.id.action_dashboard
        }
}

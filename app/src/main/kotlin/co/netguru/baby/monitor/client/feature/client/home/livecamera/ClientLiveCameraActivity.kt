package co.netguru.baby.monitor.client.feature.client.home.livecamera

import android.os.Bundle
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.extensions.inTransaction
import dagger.android.support.DaggerAppCompatActivity

class ClientLiveCameraActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_live_camera)
        if (savedInstanceState == null) {
            supportFragmentManager.inTransaction {
                add(R.id.clientLiveCameraFl, ClientLiveCameraFragment())
            }
        }
    }
}

package co.netguru.baby.monitor.client.feature.client.home.dashboard

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.extensions.getColorCompat
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_client_dashboard.*

class ClientDashboardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_client_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlideApp
                .with(requireContext())
                .load(ColorDrawable(requireContext().getColorCompat(R.color.place_holder_grey)))
                .apply(RequestOptions.circleCropTransform())
                .into(clientHomeBabyIv)
    }
}

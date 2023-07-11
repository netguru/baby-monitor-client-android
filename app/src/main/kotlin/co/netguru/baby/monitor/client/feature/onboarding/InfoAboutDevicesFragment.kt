package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.databinding.FragmentInfoAboutDevicesBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen

class InfoAboutDevicesFragment : BaseFragment(R.layout.fragment_info_about_devices) {
    override val screen: Screen = Screen.INFO_ABOUT_DEVICES
    private lateinit var binding: FragmentInfoAboutDevicesBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoAboutDevicesBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            specifyDeviceDescriptionTv.text = Html.fromHtml(getString(R.string.sync_description))
            specifyDeviceBtn.setOnClickListener {
                findNavController().navigate(R.id.infoAboutDevicesToSpecifyDevice)
            }
        }
    }
}

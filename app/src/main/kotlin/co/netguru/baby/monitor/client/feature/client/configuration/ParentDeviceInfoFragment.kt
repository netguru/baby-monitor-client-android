package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.databinding.FragmentParentDeviceInfoBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen

class ParentDeviceInfoFragment : BaseFragment(R.layout.fragment_parent_device_info) {
    override val screen: Screen = Screen.PARENT_DEVICE_INFO
    private lateinit var binding: FragmentParentDeviceInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParentDeviceInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            secondAppButtonCtrl.setOnClickListener {
                findNavController().navigate(R.id.secondAppInfoToServiceDiscovery)
            }

            secondAppInfoBackIv.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}
package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.databinding.FragmentSpecifyDeviceBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen

class SpecifyDeviceFragment : BaseFragment(R.layout.fragment_specify_device) {
    override val screen: Screen = Screen.SPECIFY_DEVICE
    private lateinit var binding : FragmentSpecifyDeviceBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpecifyDeviceBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            babyCtl.setOnClickListener {
                findNavController().navigate(R.id.specifyDeviceToFeatureD)
            }
            parentCtl.setOnClickListener {
                findNavController().navigate(R.id.specifyDeviceToParentDeviceInfo)
            }
        }
    }
}

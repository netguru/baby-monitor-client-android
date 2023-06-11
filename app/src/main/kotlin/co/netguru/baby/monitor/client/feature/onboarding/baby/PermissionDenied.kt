package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.databinding.FragmentDeniedPermissionBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen

class PermissionDenied : BaseFragment(R.layout.fragment_denied_permission) {
    override val screen: Screen = Screen.PERMISSION_DENIED
    private lateinit var binding : FragmentDeniedPermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeniedPermissionBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deniedRetryButtonCtrl.setOnClickListener {
            findNavController().popBackStack(R.id.connectWiFi, false)
        }
        binding.deniedSureButtonCtrl.setOnClickListener {
            requireActivity().finish()
        }
    }
}

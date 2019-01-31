package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_all_done.*

class AllDoneFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_all_done

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addDoneCtrl.setOnClickListener {
            findNavController().navigate(R.id.allDoneToClientHome)
            requireActivity().finish()
        }
        allDoneBackIv.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}

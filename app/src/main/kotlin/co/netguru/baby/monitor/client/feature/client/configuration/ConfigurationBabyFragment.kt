package co.netguru.baby.monitor.client.feature.client.configuration

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.common.extensions.trimmedText
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.client.home.dashboard.ClientDashboardFragment
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_configuring_baby.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import javax.inject.Inject

class ConfigurationBabyFragment : DaggerFragment() {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ClientHomeViewModel::class.java]
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_configuring_baby, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getChildrenList().observe(this, Observer {
            setupView()
        })
        viewModel.selectedChild.observe(this, Observer {
            GlideApp.with(requireContext())
                    .load(it?.image ?: "")
                    .apply(RequestOptions.circleCropTransform())
                    .into(configuringBabyIv)
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE &&
                requireContext().allPermissionsGranted(ClientDashboardFragment.PERMISSIONS)) {
            getPictureWithEasyPicker()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        EasyImage.handleActivityResult(requestCode, resultCode, data, requireActivity(), object : EasyImage.Callbacks {
            override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
                configuringBabyPb.setVisible(true)
                configuringBabySaveBtn.isEnabled = false
                viewModel.saveImage(requireContext(), imageFile).observe(this@ConfigurationBabyFragment, Observer {
                    configuringBabyPb.setVisible(false)
                    configuringBabySaveBtn.isEnabled = true
                })
            }

            override fun onImagePickerError(e: java.lang.Exception?, source: EasyImage.ImageSource?, type: Int) = Unit
            override fun onCanceled(source: EasyImage.ImageSource?, type: Int) = Unit
        })
    }

    private fun setupView() {
        configuringBabySaveBtn.setOnClickListener {
            if (configuringBabyBabyNameMet.trimmedText.isEmpty()) return@setOnClickListener
            viewModel.updateChildName(configuringBabyBabyNameMet.trimmedText)

            findNavController().navigate(R.id.actionConfiguringClientHome)
            requireActivity().finish()
        }
        configuringBabyIv.setOnClickListener {
            if (requireContext().allPermissionsGranted(ClientDashboardFragment.PERMISSIONS)) {
                getPictureWithEasyPicker()
            } else {
                requestPermissions(ClientDashboardFragment.PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            }
        }
    }

    private fun getPictureWithEasyPicker() {
        EasyImage.openChooserWithDocuments(
                this,
                getString(R.string.dialog_title_choose_source),
                EasyImage.REQ_SOURCE_CHOOSER
        )
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 3
    }
}

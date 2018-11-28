package co.netguru.baby.monitor.client.feature.client.home.dashboard

import android.Manifest
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
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.common.extensions.*
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_client_dashboard.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import javax.inject.Inject

class ClientDashboardFragment : DaggerFragment() {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_client_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        getData()
    }

    private fun setupView() {
        clientHomeLiveCameraIbtn.setOnClickListener {
            findNavController().navigate(R.id.actionDashboardToLiveCam)
        }
        clientHomeTalkIbtn.setOnClickListener {
            findNavController().navigate(R.id.actionDashboardToTalk)
        }
        clientHomeBabyIv.setOnClickListener {
            if (requireContext().allPermissionsGranted(PERMISSIONS)) {
                getPictureWithEasyPicker()
            } else {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            }
        }
        clientHomeBabyNameMet.afterTextChanged {
            if (it.trim() != viewModel.selectedChild.value?.name) {
                viewModel.updateChildName(it.trim())
            }
        }
    }

    private fun getData() {
        viewModel.selectedChild.observe(this, Observer {
            it ?: return@Observer
            clientHomeAddPhotoTv.setVisible(it.image.isNullOrEmpty())

            GlideApp.with(requireContext())
                    .load(it.image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(clientHomeBabyIv)
            if (clientHomeBabyNameMet.text.toString().trim() != it.name?.trim()) {
                clientHomeBabyNameMet.setText(it.name)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        EasyImage.handleActivityResult(requestCode, resultCode, data, requireActivity(), object : EasyImage.Callbacks {
            override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
                clientHomeDashboardPb.setVisible(true)
                viewModel.saveImage(requireContext(), imageFile).observe(this@ClientDashboardFragment, Observer {
                    clientHomeDashboardPb.setVisible(false)
                })
            }

            override fun onImagePickerError(e: java.lang.Exception?, source: EasyImage.ImageSource?, type: Int) = Unit
            override fun onCanceled(source: EasyImage.ImageSource?, type: Int) = Unit
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE &&
                requireContext().allPermissionsGranted(PERMISSIONS)) {
            getPictureWithEasyPicker()
        } else {
            showSnackbarMessage(R.string.snackbar_permissions_not_granted) {
                setAction(R.string.settings) { startAppSettings() }
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
        internal val PERMISSIONS = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val PERMISSIONS_REQUEST_CODE = 123
    }
}

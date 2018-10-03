package co.netguru.baby.monitor.client.feature.client.home.dashboard

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.common.extensions.inTransaction
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.client.home.livecamera.ClientLiveCameraFragment
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
            fragmentManager?.inTransaction {
                replace(R.id.clientHomeFrameLayout, ClientLiveCameraFragment.newInstance())
                addToBackStack(null)
            }
        }
        clientHomeBabyIv.setOnClickListener {
            if (requireContext().allPermissionsGranted(PERMISSION_CAMERA)) {
                getPictureWithEasyPicker()
            } else {
                requestPermissions(PERMISSION_CAMERA, PERMISSIONS_CAMERA_REQUEST_CODE)
            }

        }
        clientHomeBabyNameMet.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s?.toString()?.trim().equals(viewModel.selectedChild.value?.name)) {
                    viewModel.updateChildName(s.toString().trim())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }

    private fun getData() {
        viewModel.selectedChild.observe(this, Observer {
            it ?: return@Observer
            clientHomeAddPhotoTv.visibility = if (!it.image.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }

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
                viewModel.saveImage(requireContext(), imageFile) {
                    Handler(Looper.getMainLooper()).post {
                        clientHomeDashboardPb.setVisible(false)
                    }
                }
            }

            override fun onImagePickerError(e: java.lang.Exception?, source: EasyImage.ImageSource?, type: Int) = Unit
            override fun onCanceled(source: EasyImage.ImageSource?, type: Int) = Unit
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requireContext().allPermissionsGranted(PERMISSION_CAMERA)) {
            getPictureWithEasyPicker()
        }
    }

    private fun getPictureWithEasyPicker() {
        EasyImage.openChooserWithDocuments(this, "Choose source", EasyImage.REQ_SOURCE_CHOOSER)

    }

    companion object {
        private val PERMISSION_CAMERA = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val PERMISSIONS_CAMERA_REQUEST_CODE = 123
    }
}

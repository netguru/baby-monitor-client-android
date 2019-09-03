package co.netguru.baby.monitor.client.feature.settings


import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.common.extensions.*
import co.netguru.baby.monitor.client.feature.client.configuration.ConfigurationViewModel
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import kotlinx.android.synthetic.main.fragment_client_settings.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import javax.inject.Inject


class ClientSettingsFragment : BaseDaggerFragment() {
    override val layoutResource = R.layout.fragment_client_settings

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy { ViewModelProviders.of(this, factory)[ConfigurationViewModel::class.java] }
    private val settingsViewModel by lazy { ViewModelProviders.of(requireActivity(), factory)[SettingsViewModel::class.java] }
    private val clientViewModel by lazy { ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rateUsBtn.setOnClickListener {
            settingsViewModel.openMarket(requireActivity())
        }

        settingsLogoutBtn.setOnClickListener {
            viewModel.clearData(requireActivity())
        }

        secondPartTv.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.company_url))))
        }

        closeIbtn.setOnClickListener {
            clientViewModel.selectedChild.value?.let { child ->
                settingsViewModel.updateChildName(childNameEt.trimmedText, child)
            }
            clientViewModel.shouldDrawerBeOpen.postValue(false)
        }

        childPhotoIv.setOnClickListener {
            takeOrChoosePhoto()
        }

        childPhotoIv.babyProfileImage(
                R.drawable.ic_select_photo_placeholder,
                BITMAP_AUTO_SIZE,
                R.color.alpha_accent,
                R.drawable.ic_select_photo_camera
        )

        clientViewModel.selectedChild.observe(this, Observer { child ->
            child ?: return@Observer

            if (!child.name.isNullOrEmpty()) {
                childNameEt.setText(child.name)
            }
            if (!child.image.isNullOrEmpty()) {
                childPhotoIv.babyProfileImage(child.image, -1f,
                        R.color.alpha_accent, R.drawable.ic_select_photo_camera)
            }
        })
        clientViewModel.saveChildNameRequired.observe(this, Observer { updateRequired ->
            if (updateRequired == true) {
                clientViewModel.selectedChild.value?.let { child ->
                    settingsViewModel.updateChildName(childNameEt.trimmedText, child)
                }
            }
        })

        childNameEt.onFocusChangeListener =
                View.OnFocusChangeListener { view: View, hasFocus: Boolean ->
                    if (!hasFocus) {
                        settingsViewModel.hideKeyboard(view, requireContext())
                    }
                }

        version.text = getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    private fun getPictureWithEasyPicker() {
        EasyImage.openChooserWithDocuments(
                this,
                getString(R.string.dialog_title_choose_source),
                EasyImage.REQ_SOURCE_CHOOSER
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        EasyImage.handleActivityResult(requestCode, resultCode, data, requireActivity(), object : EasyImage.Callbacks {
            override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
                imageFile ?: return
                clientViewModel.selectedChild.value?.let { child ->
                    settingsViewModel.saveImage(requireContext(), imageFile, child)
                }
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
            showSnackbarMessage(R.string.hockeyapp_error_no_external_storage_permission) {
                setAction(R.string.settings) { startAppSettings() }
            }
        }
    }

    private fun takeOrChoosePhoto() {
        if (requireContext().allPermissionsGranted(PERMISSIONS)) {
            getPictureWithEasyPicker()
        } else {
            requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }

    companion object {
        internal val PERMISSIONS = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val PERMISSIONS_REQUEST_CODE = 123
    }

}

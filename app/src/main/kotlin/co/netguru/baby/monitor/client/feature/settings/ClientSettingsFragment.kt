package co.netguru.baby.monitor.client.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.*
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_client_settings.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ClientSettingsFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_client_settings

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val configurationViewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ConfigurationViewModel::class.java]
    }
    private val settingsViewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[SettingsViewModel::class.java]
    }
    private val clientViewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }
    private val viewDisposables = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        setupObservers()
        setupBabyDetails()
        setupNoiseDetectionSeekbar()

        version.text =
            getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    private fun setupBabyDetails() {
        childPhotoIv.babyProfileImage(
            R.drawable.ic_select_photo_placeholder,
            BITMAP_AUTO_SIZE,
            R.color.alpha_accent,
            R.drawable.ic_select_photo_camera
        )

        childNameEt.onFocusChangeListener =
            View.OnFocusChangeListener { view: View, hasFocus: Boolean ->
                if (!hasFocus) {
                    settingsViewModel.hideKeyboard(view, requireContext())
                    if (childNameEt.text.isNullOrBlank()) {
                        childNameEt.text?.clear()
                    }
                    settingsViewModel.updateChildName(childNameEt.text.toString())
                }
            }
    }

    private fun setupButtons() {
        rateUsBtn.setOnClickListener {
            settingsViewModel.openMarket(requireActivity())
        }

        resetAppBtn.setOnClickListener {
            configurationViewModel.resetApp(clientViewModel)
        }

        secondPartTv.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.company_url))))
        }

        closeIbtn.setOnClickListener {
            clientViewModel.shouldDrawerBeOpen.postValue(false)
        }

        childPhotoIv.setOnClickListener {
            takeOrChoosePhoto()
        }

        voiceAnalysisRadioButtons.setOnCheckedChangeListener(voiceAnalysisCheckChangedListener())
    }

    private fun voiceAnalysisCheckChangedListener(): RadioGroup.OnCheckedChangeListener {
        return RadioGroup.OnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.noiseDetectionOption -> VoiceAnalysisOption.NOISE_DETECTION
                R.id.machineLearningOption -> VoiceAnalysisOption.MACHINE_LEARNING
                else -> null
            }?.let {
                configurationViewModel.chooseVoiceAnalysisOption(
                    clientViewModel,
                    it
                )
            }
        }
    }

    private fun setupObservers() {
        clientViewModel.selectedChildLiveData.observeNonNull(viewLifecycleOwner) { child ->
            if (!child.name.isNullOrEmpty()) {
                childNameEt.setText(child.name)
            }
            if (!child.image.isNullOrEmpty()) {
                childPhotoIv.babyProfileImage(
                    child.image, -1f,
                    R.color.alpha_accent, R.drawable.ic_select_photo_camera
                )
            }
            checkVoiceAnalysisOption(resolveOption(child.voiceAnalysisOption))
            noiseDetectionGroup.isVisible =
                child.voiceAnalysisOption == VoiceAnalysisOption.NOISE_DETECTION
            noiseDetectionSeekBar.progress = child.noiseLevel
        }
        configurationViewModel.resetState.observe(viewLifecycleOwner, Observer { resetState ->
            when (resetState) {
                is ChangeState.InProgress -> setupResetButton(true)
                is ChangeState.Failed -> setupResetButton(false)
            }
        })

        configurationViewModel.voiceAnalysisOptionState.observe(
            viewLifecycleOwner,
            Observer { voiceAnalysisChangeState ->
                setupVoiceAnalysisRadioButtons(voiceAnalysisChangeState)
            })

        configurationViewModel.noiseLevelState.observe(viewLifecycleOwner,
            Observer { noiseLevelState ->
                setupNoiseLevelSeekbar(noiseLevelState)
            })
    }

    private fun setupNoiseLevelSeekbar(noiseLevelState: Pair<ChangeState, Int?>) {
        noiseDetectionSeekBar.isEnabled = noiseLevelState.first != ChangeState.InProgress
        if (noiseLevelState.first == ChangeState.Failed) setPreviousValue(
            noiseLevelState
        )
        hideNoiseChangeProgressAnimation(noiseLevelState)
        noiseLevelProgress.setState(noiseLevelState)
    }

    private fun setPreviousValue(noiseLevelState: Pair<ChangeState, Int?>) {
        noiseLevelState.second?.let {
            noiseDetectionSeekBar.progress = it
        }
    }

    private fun hideNoiseChangeProgressAnimation(noiseLevelState: Pair<ChangeState, Int?>) {
        if (noiseLevelState.first == ChangeState.Completed || noiseLevelState.first == ChangeState.Failed) {
            viewDisposables += Single.just(Unit)
                .delay(
                    resources.getInteger(R.integer.done_fail_animation_duration).toLong(),
                    TimeUnit.MILLISECONDS,
                    Schedulers.io()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ ->
                    view?.run {
                        (this as? MotionLayout)?.transitionToStart()
                    }
                }
        }
    }

    private fun setupVoiceAnalysisRadioButtons(voiceAnalysisChangeState: Pair<ChangeState, VoiceAnalysisOption?>) {
        machineLearningOption.isEnabled = voiceAnalysisChangeState.first != ChangeState.InProgress
        noiseDetectionOption.isEnabled = voiceAnalysisChangeState.first != ChangeState.InProgress
        voiceAnalysisChangeState.second?.run {
            checkVoiceAnalysisOption(resolveOption(this))
        }
    }

    private fun resolveOption(
        voiceAnalysisOption: VoiceAnalysisOption
    ): Int {
        return when (voiceAnalysisOption) {
            VoiceAnalysisOption.MACHINE_LEARNING -> R.id.machineLearningOption
            VoiceAnalysisOption.NOISE_DETECTION -> R.id.noiseDetectionOption
        }
    }

    private fun checkVoiceAnalysisOption(optionToSet: Int) {
        voiceAnalysisRadioButtons.apply {
            setOnCheckedChangeListener(null)
            voiceAnalysisRadioButtons.check(optionToSet)
            setOnCheckedChangeListener(voiceAnalysisCheckChangedListener())
        }
    }

    private fun setupResetButton(resetInProgress: Boolean) {
        resetAppBtn.apply {
            isClickable = !resetInProgress
            text = if (resetInProgress) "" else resources.getString(R.string.reset)
        }
        resetProgressBar.isVisible = resetInProgress
    }

    private fun getPictureWithEasyPicker() {
        EasyImage.openChooserWithDocuments(
            this,
            getString(R.string.dialog_title_choose_source),
            EasyImage.REQ_SOURCE_CHOOSER
        )
    }

    private fun setupNoiseDetectionSeekbar() {
        blockDrawerMovement()
        viewDisposables += Observable.create<SeekBarState> { emitter ->
            noiseDetectionSeekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) emitter.onNext(SeekBarState.ProgressChange(progress))
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    emitter.onNext(SeekBarState.StartTracking(seekBar?.progress ?: 0))
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    emitter.onNext(
                        SeekBarState.EndTracking(
                            seekBar?.progress ?: configurationViewModel.noiseLevelInitialValue
                        )
                    )
                }
            })
            emitter.setCancellable { noiseDetectionSeekBar.setOnSeekBarChangeListener(null) }
        }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { handleSeekbarState(it) }
    }

    private fun blockDrawerMovement() {
        noiseDetectionSeekBar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> // Disallow Drawer to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP -> // Allow Drawer to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(false)
            }
            // Handle seekbar touch events.
            v.onTouchEvent(event)
            true
        }
    }

    private fun handleSeekbarState(seekBarState: SeekBarState) {
        when (seekBarState) {
            is SeekBarState.StartTracking -> {
                configurationViewModel.noiseLevelInitialValue = seekBarState.initialValue
                (requireView() as? MotionLayout)?.transitionToEnd()
            }
            is SeekBarState.EndTracking -> {
                configurationViewModel.changeNoiseLevel(
                    clientViewModel,
                    seekBarState.endValue
                )
            }
            is SeekBarState.ProgressChange
            -> noiseLevelProgress.setState(null to seekBarState.progress)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        EasyImage.handleActivityResult(
            requestCode,
            resultCode,
            data,
            requireActivity(),
            object : EasyImage.Callbacks {
                override fun onImagePicked(
                    imageFile: File?,
                    source: EasyImage.ImageSource?,
                    type: Int
                ) {
                    imageFile ?: return
                    clientViewModel.selectedChildLiveData.value?.let { child ->
                        settingsViewModel.saveImage(requireContext(), imageFile, child)
                    }
                }

                override fun onImagePickerError(
                    e: java.lang.Exception?,
                    source: EasyImage.ImageSource?,
                    type: Int
                ) = Unit

                override fun onCanceled(source: EasyImage.ImageSource?, type: Int) = Unit
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE &&
            requireContext().allPermissionsGranted(PERMISSIONS)
        ) {
            getPictureWithEasyPicker()
        } else {
            showSnackbarMessage(R.string.no_external_storage_permission) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        viewDisposables.dispose()
    }

    companion object {
        internal val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val PERMISSIONS_REQUEST_CODE = 123
    }
}

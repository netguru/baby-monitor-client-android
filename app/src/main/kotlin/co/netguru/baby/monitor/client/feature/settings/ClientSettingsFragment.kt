package co.netguru.baby.monitor.client.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.BITMAP_AUTO_SIZE
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.common.extensions.babyProfileImage
import co.netguru.baby.monitor.client.common.extensions.daggerParentActivityViewModel
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.common.extensions.startAppSettings
import co.netguru.baby.monitor.client.databinding.FragmentClientSettingsBinding
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider

class ClientSettingsFragment : BaseFragment(R.layout.fragment_client_settings) {
    private lateinit var binding: FragmentClientSettingsBinding

    private val configurationViewModel by daggerParentActivityViewModel { configurationViewModelProvider }
    private val settingsViewModel by daggerParentActivityViewModel { settingsViewModelProvider }
    private val clientViewModel by daggerParentActivityViewModel { clientViewModelProvider }

    @Inject
    lateinit var configurationViewModelProvider : Provider<ConfigurationViewModel>

    @Inject
    lateinit var settingsViewModelProvider : Provider<SettingsViewModel>

    @Inject
    lateinit var clientViewModelProvider : Provider<ClientHomeViewModel>

    private val viewDisposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientSettingsBinding.inflate(layoutInflater)
        appComponent.inject(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        setupObservers()
        setupBabyDetails()
        setupNoiseDetectionSeekbar()

        binding.version.text =
            getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    private fun setupBabyDetails() {
        with(binding) {
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
    }

    private fun setupButtons() {
        with(binding) {
            rateUsBtn.setOnClickListener {
                settingsViewModel.openMarket(requireActivity())
            }

            resetAppBtn.setOnClickListener {
                configurationViewModel.resetApp(clientViewModel)
            }

            secondPartTv.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.company_url))
                    )
                )
            }

            closeIbtn.setOnClickListener {
                clientViewModel.shouldDrawerBeOpen.postValue(false)
            }

            childPhotoIv.setOnClickListener {
                takeOrChoosePhoto()
            }

            voiceAnalysisRadioButtons.setOnCheckedChangeListener(voiceAnalysisCheckChangedListener())
        }
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
            with(child) {
                binding.apply {
                    if (name.isNullOrEmpty()) {
                        childNameEt.setText(name)
                    }
                    if (image.isNullOrEmpty()) {
                        childPhotoIv.babyProfileImage(
                            image, -1f,
                            R.color.alpha_accent, R.drawable.ic_select_photo_camera
                        )
                    }
                    checkVoiceAnalysisOption(resolveOption(voiceAnalysisOption))
                    noiseDetectionGroup.isVisible =
                        voiceAnalysisOption == VoiceAnalysisOption.NOISE_DETECTION
                    noiseDetectionSeekBar.progress = noiseLevel
                }
            }
            configurationViewModel.resetState.observe(viewLifecycleOwner, Observer { resetState ->
                when (resetState) {
                    is ChangeState.InProgress -> setupResetButton(true)
                    is ChangeState.Failed -> setupResetButton(false)
                    else -> {}
                }
            })
        }

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
        with(binding) {
            noiseDetectionSeekBar.isEnabled = noiseLevelState.first != ChangeState.InProgress
            if (noiseLevelState.first == ChangeState.Failed) setPreviousValue(
                noiseLevelState
            )
            hideNoiseChangeProgressAnimation(noiseLevelState)
            noiseLevelProgress.setState(noiseLevelState)
        }
    }

    private fun setPreviousValue(noiseLevelState: Pair<ChangeState, Int?>) {
        noiseLevelState.second?.let {
            binding.noiseDetectionSeekBar.progress = it
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
        with(binding) {
            machineLearningOption.isEnabled =
                voiceAnalysisChangeState.first != ChangeState.InProgress
            noiseDetectionOption.isEnabled =
                voiceAnalysisChangeState.first != ChangeState.InProgress
            voiceAnalysisChangeState.second?.run {
                checkVoiceAnalysisOption(resolveOption(this))
            }
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
        with(binding) {
            voiceAnalysisRadioButtons.apply {
                setOnCheckedChangeListener(null)
                voiceAnalysisRadioButtons.check(optionToSet)
                setOnCheckedChangeListener(voiceAnalysisCheckChangedListener())
            }
        }
    }

    private fun setupResetButton(resetInProgress: Boolean) {
        with(binding) {
            resetAppBtn.apply {
                isClickable = !resetInProgress
                text = if (resetInProgress) "" else resources.getString(R.string.reset)
            }
            resetProgressBar.isVisible = resetInProgress
        }
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
        with(binding) {
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
    }

    private fun blockDrawerMovement() {
        binding.noiseDetectionSeekBar.setOnTouchListener { v, event ->
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
            -> binding.noiseLevelProgress.setState(null to seekBarState.progress)
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

package com.ecreditpal.danaflash.ui.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableInt
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.databinding.FragmentOcrBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OcrFragment : BaseFragment() {
    private lateinit var binding: FragmentOcrBinding
    private val captureStep = ObservableInt(STEP_START)

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private var photoUri: Uri? = null

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOcrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isPhoto = activity?.intent?.getBooleanExtra(CameraActivity.KEY_PHOTO, false) ?: false

        binding.apply {
            step = captureStep
            ocrMode = isPhoto.not()

            if (isPhoto) {
                val lp = viewFinder.layoutParams as ConstraintLayout.LayoutParams
                lp.marginStart = 0
                lp.marginEnd = 0
                lp.dimensionRatio = null
            }
            back.setOnClickListener { activity?.finish() }
            capture.setOnClickListener {
                takePhoto()
            }
            cancel.setOnClickListener {
                captureStep.set(STEP_START)
                if (isPhoto.not()) {
                    binding.image.setImageResource(R.drawable.pic_ocr_border)
                }
                deleteLastPhoto()
            }
            ok.setOnClickListener {
                val input = activity?.intent?.getStringExtra(CameraActivity.KEY_JSON)
                activity?.setResult(
                    Activity.RESULT_OK,
                    Intent().apply {
                        putExtra(EXTRA_OCR_INPUT_STRING, input)
                        putExtra(EXTRA_OCR_PHOTO_URI, photoUri)
                    }
                )
                activity?.finish()
            }

            if (isPhoto) {
                //normal photo
                val lp = back.layoutParams as ConstraintLayout.LayoutParams
                lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                lp.endToEnd = -1
            } else {
                //ocr
                //rotate to horizontal ui
                back.rotation = 90f
                ok.rotation = 90f
            }
        }

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        outputDirectory = File(context?.filesDir, "picture").also {
            if (it.exists().not()) {
                it.mkdir()
            }
        }

        startCamera(view.context)
    }

    private fun startCamera(context: Context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetResolution(Size(binding.grayBg.width, binding.grayBg.height))
                .build()


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e("TakePictureFragment", "Use case binding failed", exc)
                activity?.finish()
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(outputDirectory, "pic_" + System.currentTimeMillis() + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        captureStep.set(STEP_CAPTURING)
        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    captureStep.set(STEP_START)
                    LogUtils.e("Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    captureStep.set(STEP_CAPTURED)
                    photoUri = output.savedUri ?: Uri.fromFile(photoFile)
                    loadImage()
                }
            })
    }

    private fun loadImage() {
        binding.image.post {
            Glide.with(this)
                .load(photoUri)
                .into(binding.image)
        }
    }

    private fun deleteLastPhoto() {
        lifecycleScope.launch(Dispatchers.IO) {
            photoUri?.let {
                kotlin.runCatching {
                    val path = it.path ?: return@launch
                    File(path).delete()
                }
            }
        }
    }

    companion object {
        const val EXTRA_OCR_PHOTO_URI = "extra_ocr_photo_uri"
        const val EXTRA_OCR_INPUT_STRING = "extra_ocr_input_string"
        const val STEP_START = 0
        const val STEP_CAPTURING = 1
        const val STEP_CAPTURED = 2
    }
}
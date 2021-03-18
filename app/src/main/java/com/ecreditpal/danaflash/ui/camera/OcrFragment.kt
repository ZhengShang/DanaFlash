package com.ecreditpal.danaflash.ui.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
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
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OcrFragment : BaseFragment() {
    private lateinit var binding: FragmentOcrBinding
    private val captureStep = ObservableInt(STEP_START)

    private var displayId: Int = -1
    private var imageCapture: ImageCapture? = null
    private var photoUri: Uri? = null

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@OcrFragment.displayId) {
                LogUtils.d("Rotation changed: ${view.display.rotation}")
                imageCapture?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }


    override fun onDestroyView() {
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()

        displayManager.unregisterDisplayListener(displayListener)
    }

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

            back.setOnClickListener { activity?.finish() }
            capture.setOnClickListener {
                takePhoto()
            }
            cancel.setOnClickListener {
                captureStep.set(STEP_START)
                if (isPhoto.not()) {
                    binding.image.setImageResource(R.drawable.pic_ocr_border)
                }
                val uri = photoUri
                deleteLastPhoto(uri)
            }
            ok.setOnClickListener {
                val input = activity?.intent?.getStringExtra(CameraActivity.KEY_JSON)?.let {
                    if (it.isEmpty()) {
                        ""
                    } else {
                        kotlin.runCatching {
                            JSONObject(it).optString("type")
                        }.getOrNull()
                    }
                } ?: ""
                activity?.setResult(
                    Activity.RESULT_OK,
                    Intent().apply {
                        putExtra(EXTRA_OCR_INPUT_STRING, input)
                        putExtra(EXTRA_OCR_PHOTO_URI, photoUri)
                    }
                )
                activity?.finish()
            }

        }

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        displayManager.registerDisplayListener(displayListener, null)

        startCamera(view.context)
    }

    private fun startCamera(context: Context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val rotation = binding.viewFinder.display.rotation

            // Preview
            val preview = Preview.Builder()
                // Set initial target rotation
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetResolution(Size(binding.grayBg.width * 2, binding.grayBg.height * 2))
                .setTargetRotation(rotation)
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
        val photoFile = File(CameraActivity.outputDirectory, "pic_" + System.currentTimeMillis() + ".jpg")

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
            kotlin.runCatching {
                Glide.with(this)
                    .load(photoUri)
                    .into(binding.image)
            }
        }
    }

    private fun deleteLastPhoto(uri: Uri?) {
        lifecycleScope.launch(Dispatchers.IO) {
            uri?.let {
                kotlin.runCatching {
                    val path = it.path ?: return@launch
                    File(path).delete()
                }
            }
        }
    }

    override fun onStop() {
        kotlin.runCatching {
            val ctx = context
            if (ctx != null) {
                ProcessCameraProvider.getInstance(ctx).get().unbindAll()
            }
        }
        super.onStop()
    }

    companion object {
        const val EXTRA_OCR_PHOTO_URI = "extra_ocr_photo_uri"
        const val EXTRA_OCR_INPUT_STRING = "extra_ocr_input_string"
        const val STEP_START = 0
        const val STEP_CAPTURING = 1
        const val STEP_CAPTURED = 2
    }
}
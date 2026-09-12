package com.example.expensetrackerapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * OCR Receipt Scanner dialog using CameraX live preview and local photo upload.
 */
class OcrScannerDialog : DialogFragment() {

    interface OcrResultCallback {
        fun onOcrResult(amount: Double, store: String, date: String)
    }

    private var detectedAmount = 0.0
    private var detectedStore  = ""
    private var detectedDate   = ""

    private lateinit var stateIdle: LinearLayout
    private lateinit var stateScanning: LinearLayout
    private lateinit var stateComplete: LinearLayout
    private lateinit var laserBar: View
    private lateinit var tvDetectedAmount: TextView
    private lateinit var tvDetectedStore: TextView
    private lateinit var tvDetectedDate: TextView
    private lateinit var btnFillAmount: Button
    private lateinit var viewFinder: PreviewView
    private lateinit var tvCameraPlaceholder: TextView
    private var imageCapture: ImageCapture? = null
    private var laserAnimator: ObjectAnimator? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(context, "Camera permission is required for live scan preview", Toast.LENGTH_LONG).show()
        }
    }

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            showState(STATE_SCANNING)
            startLaserAnimation()
            runMlKitOcr(InputImage.fromFilePath(requireContext(), it))
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.color.card_bg)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_ocr_scanner, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stateIdle = view.findViewById(R.id.stateIdle)
        stateScanning = view.findViewById(R.id.stateScanning)
        stateComplete = view.findViewById(R.id.stateComplete)
        laserBar = view.findViewById(R.id.laserBar)
        tvDetectedAmount = view.findViewById(R.id.tvDetectedAmount)
        tvDetectedStore = view.findViewById(R.id.tvDetectedStore)
        tvDetectedDate = view.findViewById(R.id.tvDetectedDate)
        btnFillAmount = view.findViewById(R.id.btnFillAmount)
        viewFinder = view.findViewById(R.id.viewFinder)
        tvCameraPlaceholder = view.findViewById(R.id.tvCameraPlaceholder)

        // Close/Cancel Buttons
        view.findViewById<ImageButton>(R.id.btnCloseOcr).setOnClickListener { dismiss() }
        view.findViewById<Button>(R.id.btnOcrCancel).setOnClickListener { dismiss() }

        // Gallery upload button
        view.findViewById<Button>(R.id.btnUploadImage).setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        // Viewfinder click to capture and scan photo
        view.findViewById<View>(R.id.layoutViewFinderContainer).setOnClickListener {
            takePhotoAndOcr()
        }

        // Scan Again Button
        view.findViewById<Button>(R.id.btnScanAgain).setOnClickListener {
            showState(STATE_IDLE)
            btnFillAmount.isEnabled = false
        }

        // Fill Amount Action
        btnFillAmount.setOnClickListener {
            val callback = activity as? OcrResultCallback
            callback?.onOcrResult(detectedAmount, detectedStore, detectedDate)
            dismiss()
        }

        // Request Camera Permission & Start CameraX preview
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        showState(STATE_IDLE)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // Hide placeholder once active
                tvCameraPlaceholder.visibility = View.GONE
            } catch (exc: Exception) {
                Toast.makeText(context, "Could not start camera feed: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhotoAndOcr() {
        val capture = imageCapture ?: return

        showState(STATE_SCANNING)
        startLaserAnimation()

        val cacheDir = requireContext().cacheDir
        val photoFile = java.io.File(cacheDir, "ocr_capture_temp.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    laserAnimator?.cancel()
                    showState(STATE_IDLE)
                    Toast.makeText(context, "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        val inputImage = InputImage.fromFilePath(requireContext(), android.net.Uri.fromFile(photoFile))
                        runMlKitOcr(inputImage)
                    } catch (e: Exception) {
                        laserAnimator?.cancel()
                        showState(STATE_IDLE)
                        Toast.makeText(context, "Image parsing failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun runMlKitOcr(image: InputImage) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                laserAnimator?.cancel()
                val text = visionText.text
                val amount = extractAmountFromText(text) ?: 0.0
                val store = extractStoreFromText(text)

                detectedAmount = amount
                detectedStore = store
                detectedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

                val symbol = CurrencyUtils.getCurrencySymbol(requireContext())
                tvDetectedAmount.text = "$symbol${String.format("%,.2f", amount)}"
                tvDetectedStore.text = store
                tvDetectedDate.text = detectedDate

                btnFillAmount.isEnabled = (amount > 0.0)
                showState(STATE_COMPLETE)
            }
            .addOnFailureListener { e ->
                laserAnimator?.cancel()
                showState(STATE_IDLE)
                Toast.makeText(context, "OCR analysis failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun extractStoreFromText(text: String): String {
        val lines = text.split("\n").filter { it.isNotBlank() }
        val validLines = lines.filter { line ->
            !line.contains("receipt", ignoreCase = true) &&
            !line.contains("invoice", ignoreCase = true) &&
            line.length > 2
        }
        return if (validLines.isNotEmpty()) validLines[0].trim() else "Unknown Store"
    }

    private fun extractAmountFromText(text: String): Double? {
        var detectedAmount: Double? = null

        // 1. Try to find a line with "Total" or "Amount" and grab the last number on that line
        val lines = text.split("\n")
        for (line in lines) {
            if (line.contains("total", ignoreCase = true) || line.contains("amount", ignoreCase = true)) {
                val amountRegex = Regex("""(\d+(?:,\d{3})*(?:\.\d+)?)""")
                val match = amountRegex.findAll(line).lastOrNull()
                if (match != null) {
                    val amount = match.value.replace(",", "").toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        detectedAmount = amount
                        break
                    }
                }
            }
        }

        // 2. Fallback: Find all numbers with 2 decimal places and return the largest one
        if (detectedAmount == null) {
            val decimalRegex = Regex("""\b(\d+(?:,\d{3})*\.\d{2})\b""")
            val decimalMatches = decimalRegex.findAll(text).toList()
            if (decimalMatches.isNotEmpty()) {
                detectedAmount = decimalMatches.mapNotNull { it.groupValues[1].replace(",", "").toDoubleOrNull() }.maxOrNull()
            }
        }

        // 3. Fallback: Look for currency symbols (including LKR)
        if (detectedAmount == null) {
            val regex = Regex("""(?:Rs|RS|rs|LKR|lkr|\$|£|€)\.?\s*(\d+(?:,\d{3})*(?:\.\d+)?)""")
            val match = regex.find(text)
            detectedAmount = match?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
        }

        // Convert the detected amount to the app's currently selected currency
        if (detectedAmount != null) {
            val isReceiptUsd = text.contains("$")
            val isAppUsd = CurrencyUtils.getSelectedCurrency(requireContext()) == CurrencyUtils.CURRENCY_USD

            if (isReceiptUsd && !isAppUsd) {
                // Receipt is $, App is LKR
                detectedAmount *= CurrencyUtils.EXCHANGE_RATE_USD_TO_LKR
            } else if (!isReceiptUsd && isAppUsd) {
                // Receipt is LKR, App is $
                detectedAmount /= CurrencyUtils.EXCHANGE_RATE_USD_TO_LKR
            }
        }

        return detectedAmount
    }

    private fun startLaserAnimation() {
        laserBar.post {
            val parentHeight = (laserBar.parent as View).height.toFloat()
            laserAnimator = ObjectAnimator.ofFloat(laserBar, "translationY", 0f, parentHeight - 8f).apply {
                duration = 700
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                start()
            }
        }
    }

    private fun showState(state: Int) {
        stateIdle.visibility = if (state == STATE_IDLE) View.VISIBLE else View.GONE
        stateScanning.visibility = if (state == STATE_SCANNING) View.VISIBLE else View.GONE
        stateComplete.visibility = if (state == STATE_COMPLETE) View.VISIBLE else View.GONE
    }

    companion object {
        private const val STATE_IDLE = 0
        private const val STATE_SCANNING = 1
        private const val STATE_COMPLETE = 2
    }
}

package com.ssafy.firstproject.util

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer

private const val TAG = "TextAnalyzer"

class TextAnalyzer(
    private val textRecognizer: TextRecognizer,
    private val onTextRecognized: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    try {
                        onTextRecognized(visionText.text)
                    } catch (e: Exception) {
                        Log.e(TAG, "Text recognition success callback failed", e)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "텍스트 인식 실패", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
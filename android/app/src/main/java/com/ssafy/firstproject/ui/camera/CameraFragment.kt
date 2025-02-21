package com.ssafy.firstproject.ui.camera

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.databinding.FragmentCameraBinding
import com.ssafy.firstproject.util.PermissionChecker
import com.ssafy.firstproject.util.TextAnalyzer
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraFragment"

class CameraFragment : BaseFragment<FragmentCameraBinding>(
    FragmentCameraBinding::bind,
    R.layout.fragment_camera
) {

    private val checker = PermissionChecker(this)
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var textRecognizer: TextRecognizer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        cameraExecutor = Executors.newSingleThreadExecutor()

        checkPermission()

        startCamera()

        binding.btnUseText.setOnClickListener {
            val recognizedText = binding.tvRecognized.text.toString()
            val action = CameraFragmentDirections.actionDestCameraToDestCheck(recognizedText)
            findNavController().navigate(action)
        }
    }

    private fun checkPermission() {
        if (!checker.checkPermission(requireContext(), arrayOf(getRequiredPermission()))) {
            checker.setOnGrantedListener { //퍼미션 획득 성공일때
                Log.d(TAG, "checkPermission: permission granted")
            }
            checker.requestPermissionLauncher.launch(arrayOf(getRequiredPermission())) // 권한없으면 창 띄움
        } else { //이미 전체 권한이 있는 경우
            Log.d(TAG, "checkPermission: permission 이미 있음")
        }
    }

    private fun getRequiredPermission(): String {
        return Manifest.permission.CAMERA
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding.pvCamera.surfaceProvider
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, TextAnalyzer(textRecognizer) { text ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            binding.tvRecognized.text = text
                            binding.btnUseText.isEnabled = text.isNotEmpty()
                        }
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "카메라 바인딩 실패", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}
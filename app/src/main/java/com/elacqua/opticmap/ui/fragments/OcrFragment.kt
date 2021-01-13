package com.elacqua.opticmap.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elacqua.opticmap.databinding.FragmentOcrBinding
import com.elacqua.opticmap.ocr.OpenCV
import com.elacqua.opticmap.ocr.TesseractOCR
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import timber.log.Timber

class OcrFragment : Fragment() {

    private var binding: FragmentOcrBinding? = null
    private var image: Bitmap? = null
    private var langFrom: String = Constant.DEFAULT_LANGUAGE
    private var langTo: String = Constant.DEFAULT_LANGUAGE
    private lateinit var mLoaderCallback: BaseLoaderCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getArgs()
        openCvReadyListen()
    }

    private fun getArgs() {
        image = arguments?.get(Constant.OCR_IMAGE_KEY) as Bitmap
        val pref = SharedPref(requireContext())
        langFrom = pref.langFrom
        langTo = pref.langTo
    }

    private fun openCvReadyListen() {
        mLoaderCallback = object : BaseLoaderCallback(activity?.applicationContext) {
            override fun onManagerConnected(status: Int) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    getTextFromImage()
                } else {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    private fun getTextFromImage() {
        if (image == null) {
            Timber.e("image is null")
            return
        }
        binding?.imgOcrPicture?.setImageBitmap(image)
        CoroutineScope(Dispatchers.Default).launch {
            val openCV = OpenCV()
            val openCvResult = openCV.getBitmap(image!!)
            val path = requireContext().getExternalFilesDir(null)?.path.toString()
            val ocrResult = TesseractOCR.getText(openCvResult, path, langFrom)
            withContext(Dispatchers.Main) {
                binding?.txtOcrResult?.text = ocrResult
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        } else {
            Timber.e("onCreate: open cv error")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.INIT_FAILED)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOcrBinding.inflate(inflater, container, false)
        return binding!!.root
    }
}
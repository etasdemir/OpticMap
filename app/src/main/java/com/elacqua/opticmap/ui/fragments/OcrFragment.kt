package com.elacqua.opticmap.ui.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elacqua.opticmap.databinding.FragmentOcrBinding
import com.elacqua.opticmap.ocr.MLKitOCRHandler
import com.elacqua.opticmap.ocr.MLTranslator
import com.elacqua.opticmap.ocr.OCRResultListener
import com.elacqua.opticmap.ocr.RecognitionOptions
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.Languages
import com.elacqua.opticmap.util.SharedPref
import com.elacqua.opticmap.util.UIState
import timber.log.Timber

class OcrFragment : Fragment() {

    private var binding: FragmentOcrBinding? = null
    private var imageUri: Uri? = null
    private var langFrom: Languages = Constant.DEFAULT_LANGUAGE
    private var langTo: Languages = Constant.DEFAULT_LANGUAGE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getArgs()
        getTextFromImage(RecognitionOptions.TRANSLATE_BLOCKS)
    }

    private fun getArgs() {
        imageUri = arguments?.get(Constant.OCR_IMAGE_KEY) as Uri
        if (imageUri != null) {
            val pref = SharedPref(requireContext())
            langFrom = Languages.getLanguageFromShortName(pref.langFrom)
            langTo = Languages.getLanguageFromShortName(pref.langTo)
        }
    }

    private fun getTextFromImage(option: RecognitionOptions) {
        if (imageUri != null) {
            UIState.isLoadingState.value = true
            MLTranslator(langFrom, langTo) {
                val ocr = MLKitOCRHandler(requireContext(), it)
                ocr.runTextRecognition(imageUri!!, option, object: OCRResultListener {
                    override fun onSuccess(bitmap: Bitmap?) {
                        if (bitmap == null) {
                            binding?.imgOcrPicture?.setImageURI(imageUri)
                        } else {
                            binding?.imgOcrPicture?.setImageBitmap(bitmap)
                        }
                        UIState.isLoadingState.value = false
                    }

                    override fun onFailure(message: String) {
                        Timber.e("OCR failed: $message")
                        binding?.imgOcrPicture?.setImageURI(imageUri)
                        UIState.isLoadingState.value = false
                    }
                })
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOcrBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroy() {
        UIState.isLoadingState.value = false
        binding = null
        super.onDestroy()
    }
}
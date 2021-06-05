package com.elacqua.opticmap.ui.fragments


import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elacqua.opticmap.databinding.FragmentOcrBinding
import com.elacqua.opticmap.ocr.MLKitOCRHandler
import com.elacqua.opticmap.ocr.MLTranslator
import com.elacqua.opticmap.ocr.OCRResult
import com.elacqua.opticmap.ocr.VisionOCRHandler
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.Languages
import com.elacqua.opticmap.util.SharedPref
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


class OcrFragment : Fragment() {

    private var binding: FragmentOcrBinding? = null
    private var image: Bitmap? = null
    private var langFrom: Languages = Constant.DEFAULT_LANGUAGE
    private var langTo: Languages = Constant.DEFAULT_LANGUAGE
    private val translator = MLTranslator()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getArgs()
        observeTranslatedText()
        getTextFromImage()
    }

    private fun observeTranslatedText() {
        translator.translatedText.observe(viewLifecycleOwner, {
            binding?.txtTranslationResult?.text = it
            translator.close()
        })
    }

    private fun getArgs() {
        image = arguments?.get(Constant.OCR_IMAGE_KEY) as Bitmap
        if (image != null) {
            binding?.imgOcrPicture?.setImageBitmap(image)
            val pref = SharedPref(requireContext())
            langFrom = Languages.getLanguageFromShortName(pref.langFrom)
            langTo = Languages.getLanguageFromShortName(pref.langTo)
        }
    }

    private fun getTextFromImage() {
        if (image != null) {
//                val ocrResult = VisionOCRHandler.getTextFromBitmap(image!!, context?.applicationContext!!)
            MLKitOCRHandler.runTextRecognition(image!!, object: OCRResult {
                override fun onSuccess(text: String) {
                    translator.translate(text, langFrom, langTo)
                    binding?.txtOcrResult?.text = text
                }

                override fun onFailure(message: String) {
                    Timber.e("OCR failed: $message")
                }
            })
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
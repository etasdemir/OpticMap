package com.elacqua.opticmap.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elacqua.opticmap.databinding.FragmentOcrBinding
import com.elacqua.opticmap.ocr.MLTranslator
import com.elacqua.opticmap.ocr.OCRHandler
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.Languages
import com.elacqua.opticmap.util.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        binding?.imgOcrPicture?.setImageBitmap(image)
        val pref = SharedPref(requireContext())
        langFrom = Languages.getLanguageFromShortName(pref.langFrom)
        langTo = Languages.getLanguageFromShortName(pref.langTo)
    }

    private fun getTextFromImage() {
        if (image != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val ocrResult = OCRHandler.getTextFromBitmap(image!!, context?.applicationContext!!)
                translator.translate(ocrResult, langFrom, langTo)
                withContext(Dispatchers.Main) {
                    binding?.txtOcrResult?.text = ocrResult
                }
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
}
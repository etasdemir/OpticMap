package com.elacqua.opticmap.ui.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.FragmentOcrBinding
import com.elacqua.opticmap.ocr.*
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.Languages
import com.elacqua.opticmap.util.SharedPref
import com.elacqua.opticmap.util.UIState
import timber.log.Timber
import java.util.*

class OcrFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var sharedPref: SharedPref
    private lateinit var tts: TextToSpeech
    private var binding: FragmentOcrBinding? = null
    private var imageUri: Uri? = null
    private var langFrom: Languages = Constant.DEFAULT_LANGUAGE
    private var langTo: Languages = Constant.DEFAULT_LANGUAGE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tts = TextToSpeech(requireContext().applicationContext, this)
        getArgs()
        handleRadioButtons()
        textToSpeech()
    }

    private fun textToSpeech() {
        binding!!.btnOcrVoice.setOnClickListener {
            if (imageUri != null) {
                UIState.isLoadingState.value = true
                MLTranslator(langFrom, langTo) {
                    val ocr = MLKitOCRHandler(requireContext(), it)
                    ocr.ocrToSpeech(imageUri!!, object: TranslateResultListener{
                        override fun onSuccess(text: String) {
                            UIState.isLoadingState.value = false
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                        }

                        override fun onFailure(message: String) {
                            Timber.e("OCR failed: $message")
                            UIState.isLoadingState.value = false
                        }
                    })
                }
            }
        }
    }

    private fun getArgs() {
        imageUri = arguments?.get(Constant.OCR_IMAGE_KEY) as Uri
        if (imageUri != null) {
            sharedPref = SharedPref(requireContext())
            langFrom = Languages.getLanguageFromShortName(sharedPref.langFrom)
            langTo = Languages.getLanguageFromShortName(sharedPref.langTo)
        }
    }

    private fun getTextFromImage(option: RecognitionOptions) {
        if (imageUri != null) {
            UIState.isLoadingState.value = true
            MLTranslator(langFrom, langTo) {
                val ocr = MLKitOCRHandler(requireContext(), it)
                ocr.runTextRecognition(imageUri!!, option, object: OCRResultListener {
                    override fun onSuccess(bitmap: Bitmap?) {
                        UIState.isLoadingState.value = false
                        if (bitmap == null) {
                            binding?.imgOcrPicture?.setImageURI(imageUri)
                        } else {
                            binding?.imgOcrPicture?.setImageBitmap(bitmap)
                        }
                    }

                    override fun onFailure(message: String) {
                        UIState.isLoadingState.value = false
                        Timber.e("OCR failed: $message")
                        binding?.imgOcrPicture?.setImageURI(imageUri)
                    }
                })
            }

        }
    }

    private fun handleRadioButtons() {
        val lastOption = sharedPref.lastSelectedRadioButton
        binding!!.radioBtnOcrBlock.setOnCheckedChangeListener { btn, _ ->
            setRadioBtnTextColor(btn as RadioButton, RecognitionOptions.TRANSLATE_BLOCKS)
        }
        binding!!.radioBtnOcrLine.setOnCheckedChangeListener { btn, _ ->
            setRadioBtnTextColor(btn as RadioButton, RecognitionOptions.TRANSLATE_LINES)
        }
        binding!!.radioBtnOcrWhole.setOnCheckedChangeListener { btn, _ ->
            setRadioBtnTextColor(btn as RadioButton, RecognitionOptions.TRANSLATE_WHOLE)
        }
        when(lastOption) {
            RecognitionOptions.TRANSLATE_BLOCKS.name -> { binding!!.radioBtnOcrBlock.isChecked = true }
            RecognitionOptions.TRANSLATE_LINES.name -> { binding!!.radioBtnOcrLine.isChecked = true }
            RecognitionOptions.TRANSLATE_WHOLE.name -> { binding!!.radioBtnOcrWhole.isChecked = true }
        }
    }

    private fun setRadioBtnTextColor(btn: RadioButton, option: RecognitionOptions) {
        if (btn.isChecked) {
            sharedPref.lastSelectedRadioButton = option.name
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            getTextFromImage(option)
        } else {
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOcrBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onPause() {
        tts.stop()
        super.onPause()
    }

    override fun onDestroy() {
        UIState.isLoadingState.value = false
        tts.shutdown()
        binding = null
        super.onDestroy()
    }

    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }
}
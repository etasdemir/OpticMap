package com.elacqua.opticmap.ui.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.FragmentOcrBinding
import com.elacqua.opticmap.ocr.*
import com.elacqua.opticmap.util.*
import com.google.mlkit.vision.text.Text
import timber.log.Timber
import java.util.*

class OcrFragment : Fragment(), TextToSpeech.OnInitListener {

    private val ocrViewModel by viewModels<OcrViewModel>()
    private lateinit var sharedPref: SharedPref
    private lateinit var tts: TextToSpeech
    private lateinit var ocr: MLKitOCRHandler
    private var binding: FragmentOcrBinding? = null
    private var imageUri: Uri? = null
    private var langFrom: Languages = Constant.DEFAULT_LANGUAGE
    private var langTo: Languages = Constant.DEFAULT_LANGUAGE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getArgs()
        initOCR()
    }

    private fun initOCR() {
        UIState.isLoadingState.value = true
        tts = TextToSpeech(requireContext().applicationContext, this)
        val translator = MLTranslator(langFrom, langTo)
        ocr = MLKitOCRHandler(translator)
        translator.downloadModel { success ->
            if (success) {
                recognizeText()
            }
            UIState.isLoadingState.value = false
        }
    }

    private fun recognizeText() {
        UIState.isLoadingState.value = true
        val img = ocr.getImageFromUri(imageUri!!, requireContext())
        ocrViewModel.recognizeText(img, ocr)
        ocrViewModel.textsOnImage.observe(viewLifecycleOwner, { texts ->
            if (texts != null) {
                textToSpeech(texts)
                handleRadioButtons(texts)
            }
            UIState.isLoadingState.value = false
        })
    }

    private fun textToSpeech(texts: Text) {
        UIState.isLoadingState.value = true
        binding!!.btnOcrVoice.setOnClickListener {
            ocr.ocrToSpeech(texts, object: TranslateResultListener{
                override fun onSuccess(text: String) {
                    UIState.isLoadingState.value = false
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }

                override fun onFailure(message: String) {
                    UIState.isLoadingState.value = false
                }
            })
        }
    }

    private fun handleRadioButtons(text: Text) {
        binding!!.radioBtnOcrBlock.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked) {
                setLastRadioButton(text, RecognitionOptions.TRANSLATE_BLOCKS)
            }
            setRadioBtnTextColor(btn as RadioButton)
        }
        binding!!.radioBtnOcrLine.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked) {
                setLastRadioButton(text, RecognitionOptions.TRANSLATE_LINES)
            }
            setRadioBtnTextColor(btn as RadioButton)
        }
        binding!!.radioBtnOcrWhole.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked) {
                setLastRadioButton(text, RecognitionOptions.TRANSLATE_WHOLE)
            }
            setRadioBtnTextColor(btn as RadioButton)
        }
        when(sharedPref.lastSelectedRadioButton) {
            RecognitionOptions.TRANSLATE_BLOCKS.name -> { binding!!.radioBtnOcrBlock.isChecked = true }
            RecognitionOptions.TRANSLATE_LINES.name -> { binding!!.radioBtnOcrLine.isChecked = true }
            RecognitionOptions.TRANSLATE_WHOLE.name -> { binding!!.radioBtnOcrWhole.isChecked = true }
        }
    }

    private fun setLastRadioButton(text: Text, option: RecognitionOptions) {
        sharedPref.lastSelectedRadioButton = option.name
        getTextFromImage(text, option)
    }

    private fun setRadioBtnTextColor(btn: RadioButton) {
        if (btn.isChecked) {
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
        }
    }

    private fun getTextFromImage(text: Text, option: RecognitionOptions) {
        if (imageUri == null) {
            return
        }
        UIState.isLoadingState.value = true
        val bitmap = getBitmapFromUri(imageUri!!, requireContext().contentResolver)
        if (imageUri != null) {
            ocr.translateText(bitmap, text, option, object: OCRResultListener<Bitmap>{
                override fun onSuccess(result: Bitmap?) {
                    UIState.isLoadingState.value = false
                    if (result == null) {
                        binding?.imgOcrPicture?.setImageURI(imageUri)
                    } else {
                        binding?.imgOcrPicture?.setImageBitmap(result)
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

    private fun getArgs() {
        imageUri = arguments?.get(Constant.OCR_IMAGE_KEY) as Uri
        if (imageUri != null) {
            sharedPref = SharedPref(requireContext())
            langFrom = Languages.getLanguageFromShortName(sharedPref.langFrom)
            langTo = Languages.getLanguageFromShortName(sharedPref.langTo)
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
        ocr.closeTranslator()
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
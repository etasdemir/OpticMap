package com.elacqua.opticmap.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elacqua.opticmap.databinding.FragmentOcrBinding
import com.elacqua.opticmap.ocr.MLTranslator
import com.elacqua.opticmap.ocr.OCRHandler
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.nio.ByteBuffer


class OcrFragment : Fragment() {

    private var binding: FragmentOcrBinding? = null
    private var image: Bitmap? = null
    private var langFrom: String = Constant.DEFAULT_LANGUAGE
    private var langTo: String = Constant.DEFAULT_LANGUAGE
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
        langFrom = pref.langFrom
        langTo = pref.langTo
    }

    private fun getTextFromImage() {
        if (image == null) {
            Timber.e("image is null")
            return
        }

        val size: Int = image!!.rowBytes * image!!.height
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(size)
        image!!.copyPixelsToBuffer(byteBuffer)
        val byteArray = byteBuffer.array()
        val imageBase64: String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

//        val byteArrayOutputStream = ByteArrayOutputStream()
//        image!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
//        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
//        val imageString = Base64.encodeToString(byteArray, Base64.NO_WRAP)

//        Timber.e("image base64: $imageBase64")

        CoroutineScope(Dispatchers.IO).launch {
//            val ocrResult = OCRHandler.getTextFromBitmap(
//                langFrom,
//                image!!,
//                requireContext().applicationContext
//            )

            val jsonBody = JSONObject()
            val requestArray = JSONArray()
            requestArray.put(JSONObject().put("image", JSONObject().put("content", "imageBase64")))
            requestArray.put(
                JSONObject()
                    .put("features",
                        JSONArray()
                            .put(JSONObject().put("type", "TEXT_DETECTION"))
                            .put(JSONObject().put("languageHints", langFrom))
                )
            )
            jsonBody.put("requests", requestArray)
            Timber.e("json: $jsonBody")
            val url = "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyAPPOTQnzosyOgy1WWn3wcvxoDQpc6xri8"
            val result = OCRHandler.sendRequest(
                url,
                OCRHandler.RequestMethods.POST,
                jsonBody.toString()
            )
            Timber.e("result: $result")
//            translator.translate(ocrResult)
//            withContext(Dispatchers.Main) {
//                binding?.txtOcrResult?.text = ocrResult
//            }
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
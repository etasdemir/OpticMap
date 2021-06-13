package com.elacqua.opticmap.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elacqua.opticmap.ocr.MLKitOCRHandler
import com.elacqua.opticmap.ocr.OCRResultListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import timber.log.Timber

class OcrViewModel : ViewModel() {

    private val _textsOnImage = MutableLiveData<Text?>()
    val textsOnImage: LiveData<Text?> = _textsOnImage

    fun recognizeText(
        image: InputImage,
        ocr: MLKitOCRHandler
    ) {
        ocr.runTextRecognition(image, object: OCRResultListener<Text> {
            override fun onSuccess(result: Text?) {
                _textsOnImage.value = result
            }

            override fun onFailure(message: String) {
                _textsOnImage.value = null
                Timber.e(message)
            }
        })
    }
}
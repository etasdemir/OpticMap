package com.elacqua.opticmap.ui.fragments

import androidx.lifecycle.*
import com.elacqua.opticmap.data.LocalRepository
import com.elacqua.opticmap.data.local.Place
import com.elacqua.opticmap.ocr.MLKitOCRHandler
import com.elacqua.opticmap.ocr.OCRResultListener
import com.elacqua.opticmap.ui.places.PlacesViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class OcrViewModel(private val localRepository: LocalRepository) : ViewModel() {

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

    fun savePlace(place: Place) {
        viewModelScope.launch(Dispatchers.IO) {
            localRepository.addPlace(place)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class OcrViewModelFactory(private val localRepository: LocalRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return OcrViewModel(localRepository) as T
    }
}
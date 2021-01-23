package com.elacqua.opticmap.ocr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*
import timber.log.Timber

class MLTranslator {
    //TODO probably memory leaks (check live data and translator.close)

    private val _translatedText = MutableLiveData<String>()
    val  translatedText : LiveData<String> = _translatedText

    private var options: TranslatorOptions = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.TURKISH)
        .build()
    private val translator: Translator = Translation.getClient(options)

    private fun downloadModel(text: String){
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Timber.e("Model Downloaded")
                translate(text)
            }
            .addOnFailureListener { exception ->
               //TODO
            }

    }

    fun translate(text : String){
        translator.translate(text)
            .addOnSuccessListener { result ->
                _translatedText.value = result
            }
            .addOnFailureListener { exception ->
                downloadModel(text)
            }
    }

    val modelManager = RemoteModelManager.getInstance()

    // Get translation models stored on the device.
    fun getTranslationModel(){
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                return@addOnSuccessListener
            }
            .addOnFailureListener {
                return@addOnFailureListener
            }
    }

    fun close(){
        translator.close()
    }

    fun downloadDesiredModel(language : String){
        val desiredModel =
            TranslateLanguage.fromLanguageTag(language)?.let { TranslateRemoteModel.Builder(it).build() }
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        if (desiredModel != null) {
            modelManager.download(desiredModel, conditions)
                .addOnSuccessListener {
                    // Model downloaded.
                    Timber.e("Model Downloaded")
                }
                .addOnFailureListener {
                    // Error.
                    Timber.e("Model Couldn't Downloaded")
                }
        }
    }
}
package com.elacqua.opticmap.ocr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*
import timber.log.Timber

class MLTranslator {
    //TODO probably memory leaks (check live data and translator.close)

    private val modelManager = RemoteModelManager.getInstance()
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
                Timber.e("downloadModel: ${exception.message}")
            }

    }

    fun translate(text : String){
        translator.translate(text)
            .addOnSuccessListener { result ->
                _translatedText.value = result
            }
            .addOnFailureListener { exception ->
                Timber.e("translate: ${exception.message}")
                downloadModel(text)
            }
    }

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

    fun downloadSelectedModel(language : String){
        val selectedModel =
            TranslateLanguage.fromLanguageTag(language)?.let { TranslateRemoteModel.Builder(it).build() }
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        selectedModel?.let {
            modelManager.download(selectedModel, conditions)
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
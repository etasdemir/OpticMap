package com.elacqua.opticmap.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.elacqua.opticmap.ocr.RecognitionOptions

@SuppressLint("CommitPrefEdits")
class SharedPref(context: Context) {
    private val _sharedPrefs: SharedPreferences =
        context.getSharedPreferences(Constant.APP_SHARED_PREFS, Context.MODE_PRIVATE)
    private val _prefsEditor: SharedPreferences.Editor = _sharedPrefs.edit()

    var langFrom: String
        get() = _sharedPrefs.getString(Constant.PREF_FROM_LANGUAGE_KEY, Constant.DEFAULT_LANGUAGE.shortName)!!
        set(value) = _prefsEditor.putString(Constant.PREF_FROM_LANGUAGE_KEY, value).apply()

    var langTo: String
        get() = _sharedPrefs.getString(Constant.PREF_TO_LANGUAGE_KEY, Constant.DEFAULT_LANGUAGE.shortName)!!
        set(value) = _prefsEditor.putString(Constant.PREF_TO_LANGUAGE_KEY, value).apply()

    var lastSelectedRadioButton: String
        get() = _sharedPrefs.getString(Constant.PREF_OCR_RADIO_BUTTON, RecognitionOptions.TRANSLATE_BLOCKS.name)!!
        set(value) = _prefsEditor.putString(Constant.PREF_OCR_RADIO_BUTTON, value).apply()
}
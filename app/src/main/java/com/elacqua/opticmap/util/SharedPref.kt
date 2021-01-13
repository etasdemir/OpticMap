package com.elacqua.opticmap.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("CommitPrefEdits")
class SharedPref(context: Context) {
    private val _sharedPrefs: SharedPreferences =
        context.getSharedPreferences(Constant.APP_SHARED_PREFS, Context.MODE_PRIVATE)
    private val _prefsEditor: SharedPreferences.Editor = _sharedPrefs.edit()

    var langFrom: String
        get() = _sharedPrefs.getString(Constant.PREF_FROM_LANGUAGE_KEY, Constant.DEFAULT_LANGUAGE)!!
        set(value) = _prefsEditor.putString(Constant.PREF_FROM_LANGUAGE_KEY, value).apply()

    var langTo: String
        get() = _sharedPrefs.getString(Constant.PREF_TO_LANGUAGE_KEY, Constant.DEFAULT_LANGUAGE)!!
        set(value) = _prefsEditor.putString(Constant.PREF_TO_LANGUAGE_KEY, value).apply()
}
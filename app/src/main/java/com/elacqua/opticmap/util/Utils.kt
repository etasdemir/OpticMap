package com.elacqua.opticmap.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData

object Constant {

    // Shared Preferences
    const val APP_SHARED_PREFS = "Shared Pref"
    const val PREF_FROM_LANGUAGE_KEY = "OCR_FROM_LANGUAGE_KEY"
    const val PREF_TO_LANGUAGE_KEY = "OCR_TO_LANGUAGE_KEY"
    const val PREF_OCR_RADIO_BUTTON = "PREF_OCR_RADIO_BUTTON"
    const val DEFAULT_OCR_RADIO_BUTTON = "OcrRadioButtonBlock"

    // Args
    const val OCR_IMAGE_KEY = "OCR_IMAGE_KEY"
    const val PHOTO_EDIT_KEY = "PHOTO_EDIT_KEY"

    const val IMAGE_PICK_INTENT_CODE = 10
    const val CAMERA_REQUEST_CODE = 11

    val DEFAULT_LANGUAGE = Languages.English
}

object UIState {
    var isLoadingState = MutableLiveData(false)
}

fun getBitmapFromUri(uri: Uri, contentResolver: ContentResolver): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(contentResolver, uri)
    } else {
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}
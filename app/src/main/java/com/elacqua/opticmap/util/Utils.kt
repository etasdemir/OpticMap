package com.elacqua.opticmap.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


object Constant {

    // Shared Preferences
    const val APP_SHARED_PREFS = "Shared Pref"
    const val PREF_FROM_LANGUAGE_KEY = "OCR_FROM_LANGUAGE_KEY"
    const val PREF_TO_LANGUAGE_KEY = "OCR_TO_LANGUAGE_KEY"
    const val PREF_OCR_RADIO_BUTTON = "PREF_OCR_RADIO_BUTTON"

    // Args
    const val OCR_IMAGE_KEY = "OCR_IMAGE_KEY"
    const val PLACE_ARG_KEY = "PLACE_ARG_KEY"

    const val IMAGE_PICK_INTENT_CODE = 10
    const val CAMERA_REQUEST_CODE = 11
    const val LOCATION_PERM_CODE = 13

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

// milliseconds since the epoch
fun getEpochTime(): String {
    return System.currentTimeMillis().toString()
}

// milliseconds since the epoch
fun getDateFromEpoch(epoch: String): String {
    val date = Date(epoch.toLong())
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(date)
}

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun byteArrayToBitmap(bytes: ByteArray): Bitmap =
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)


package com.elacqua.opticmap.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.elacqua.opticmap.R
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.TrainedDataDownloader
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        downloadTrainedData("eng")
        takeImageFromGallery()
    }

    private fun takeImageFromGallery() {
        val photoPickIntent = Intent(Intent.ACTION_PICK)
        photoPickIntent.type = "image/*"
        startActivityForResult(photoPickIntent, Constant.IMAGE_PICK_INTENT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == Constant.IMAGE_PICK_INTENT_CODE) {
            try {
                val imageUri: Uri? = data?.data
                imageUri?.let { uri ->
                    val imageStream = contentResolver.openInputStream(uri)
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    findViewById<ImageView>(R.id.imageView).setImageBitmap(selectedImage)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun downloadTrainedData(language: String) {
        val isConnected = checkInternetStatus()
        if (isConnected) {
            val trainedDataDownloader = TrainedDataDownloader()
            trainedDataDownloader.download(this, language)
        } else {
            showNoInternetSnackbar()
        }
    }

    @Suppress("DEPRECATION")
    private fun checkInternetStatus(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            return activeNetwork?.isConnectedOrConnecting ?: false
        }
    }

    private fun showNoInternetSnackbar() {
        Snackbar.make(
            findViewById(R.id.constraintLayout_MainActivity),
            R.string.home_no_internet,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.home_open_settings) {
                openSettings()
            }
            .show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivity(intent)
    }
}
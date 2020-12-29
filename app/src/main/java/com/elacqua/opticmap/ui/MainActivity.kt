package com.elacqua.opticmap.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.elacqua.opticmap.R
import com.elacqua.opticmap.util.TrainedDataDownloader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        downloadTrainedData("eng")

    }

    private fun downloadTrainedData(language: String) {
        val isConnected = checkInternetStatus()
        if (isConnected) {
            val trainedDataDownloader = TrainedDataDownloader()
            trainedDataDownloader.download(this, language)
        } else {
            requestConnectNetwork()
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

    private fun requestConnectNetwork() {
        openSettings()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivity(intent)
    }
}
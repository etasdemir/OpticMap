package com.elacqua.opticmap.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.JobIntentService
import com.elacqua.opticmap.R
import timber.log.Timber
import java.io.File

class TrainedDataDownloader : JobIntentService() {
    private var DATA_PATH = ""

    fun download(context: Context, language: String) {
        DATA_PATH = context.getExternalFilesDir(null)?.path.toString()
        val filePath = "$DATA_PATH$TESS_DATA/$language$TRAINED_DATA_EXT"
        val dataUrl = "$baseUrl$language$TRAINED_DATA_EXT"
        createDirIfNotExist("$DATA_PATH$TESS_DATA")

        if (!isFileExist(filePath)) {
            val intent = Intent(context, TrainedDataDownloader::class.java)
                .putExtra(DOWNLOAD_PATH_KEY, dataUrl)
                .putExtra(DESTINATION_PATH_KEY, filePath)
            enqueueWork(context, TrainedDataDownloader::class.java, DOWNLOAD_JOB_ID, intent)
        } else {
            Timber.e("Trained data file exist of the selected language")
        }
    }

    override fun onHandleWork(intent: Intent) {
        val url = (intent.extras?.get(DOWNLOAD_PATH_KEY) ?: "") as String
        val path = (intent.extras?.get(DESTINATION_PATH_KEY) ?: "") as String
        downloadTrainedData(url, path)
    }

    private fun downloadTrainedData(url: String, path: String) {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.apply {
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setTitle(getString(R.string.download_notification))
            setDestinationUri(Uri.parse("file://$path"))
        }
        (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }

    private fun isFileExist(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }

    private fun createDirIfNotExist(path: String) {
        val dir = File(path)
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Timber.e("createDirIfNotExist: Directory not created. $path")
            } else {
                Timber.d("createDirIfNotExist: Directory created. $path")
            }
        }
    }

    companion object {
        private const val baseUrl = "https://github.com/tesseract-ocr/tessdata/raw/master/"
        private const val TESS_DATA = "/tessdata"
        private const val TRAINED_DATA_EXT = ".traineddata"
        private const val DOWNLOAD_PATH_KEY = "DOWNLOAD_PATH_KEY"
        private const val DESTINATION_PATH_KEY = "DESTINATION_PATH_KEY"
        private const val DOWNLOAD_JOB_ID = 1000
    }

}
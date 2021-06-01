package com.elacqua.opticmap.ocr

import android.R.attr.data
import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

object OCRHandler {
    private lateinit var textRecognizer: TextRecognizer

    fun getTextFromBitmap(language: String, image: Bitmap, appContext: Context): String {
        if (!this::textRecognizer.isInitialized) {
            textRecognizer = TextRecognizer.Builder(appContext).build()
        }
        var result = ""
        if (!textRecognizer.isOperational) {
            Timber.e("OCRHandler::getTextFromBitmap TextRecognizer is not operational ")
        } else {
            val frame = Frame.Builder().setBitmap(image).build()
            val items = textRecognizer.detect(frame)
            val stringBuilder = StringBuilder()
            for (i in 0 until items.size()) {
                val item = items[i]
                stringBuilder.append(item.value + "\n")
            }
            result = stringBuilder.toString()
        }
        return result
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun sendRequest(
        _url: String, requestMethod: RequestMethods,
        jsonBody: String = "",
        params: Map<String, String> = emptyMap()
    ): String {
        val url = URL(_url)
        var result = ""
        withContext(Dispatchers.IO) {
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.doInput = true
            urlConnection.doOutput = true
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("Accept", "application/json")
            val wr = OutputStreamWriter(urlConnection.outputStream)
            wr.write(jsonBody)
            wr.flush()
            setPostParams(urlConnection, params)
            try {
                urlConnection.requestMethod = requestMethod.name
                Timber.e("req: ${requestMethod.name}")
                val responseCode = urlConnection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val input = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    var inputLine: String?
                    val response = StringBuffer()
                    while (input.readLine().also { inputLine = it } != null) {
                        response.append(inputLine)
                    }
                    input.close()
                    result = response.toString()
                } else {
                    Timber.e("sendRequest: response code: $responseCode")
                }
            } catch (e: Exception) {
                Timber.e("sendRequest: ${e.stackTrace}")
            } finally {
                urlConnection.disconnect()
            }
        }
        return result
    }

    private fun setPostParams(urlConnection: HttpURLConnection, params: Map<String, String>): String {
        val os = urlConnection.outputStream
        val writer = BufferedWriter(
            OutputStreamWriter(os, "UTF-8")
        )
        val result = StringBuilder()
        var first = true
        for ((key, value) in params.entries) {
            if (first) {
                first = false
            } else {
                result.append("&")
            }
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value, "UTF-8"))
        }
        writer.write(result.toString())
        writer.flush()
        writer.close()
        os.close()
        return result.toString()
    }

    enum class RequestMethods {
        GET, POST, PUT
    }
}
package com.elacqua.opticmap.util

import android.content.Context

object Utility {
    fun getRootPath (context: Context) = context.getExternalFilesDir(null)?.path.toString()

}
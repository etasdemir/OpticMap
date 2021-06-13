package com.elacqua.opticmap.data.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(val address: String = "", val city: String = "", val country: String = ""): Parcelable
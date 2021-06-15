package com.elacqua.opticmap.ui.places

import com.elacqua.opticmap.data.local.Place

interface PlaceClickListener {
    fun onPlaceClick(place: Place)
}
package com.elacqua.opticmap.data

import android.content.Context
import android.location.Geocoder
import com.elacqua.opticmap.data.local.Address
import com.elacqua.opticmap.data.local.Place
import com.elacqua.opticmap.data.local.PlacesDao
import java.util.*

class LocalRepository(private val placesDao: PlacesDao) {

    suspend fun getAllPlaces(): List<Place> = placesDao.getAllPlaces()

    suspend fun deletePlaces(place: Place) = placesDao.deletePlace(place)

    suspend fun addPlace(place: Place): Long = placesDao.addPlace(place)

    fun getAddress(context: Context, lat: Double, long: Double): Address? {
        val geoCoder = Geocoder(context, Locale.getDefault())
        val addresses = geoCoder.getFromLocation(lat, long, 1)
        return if (addresses.size > 0) {
            val adr = addresses[0]
            Address(
                adr.subLocality ?: "",
                adr.locality ?: Locale.getDefault().country,
                adr.getAddressLine(2) ?: Locale.getDefault().country
            )
        } else {
            null
        }
    }

}
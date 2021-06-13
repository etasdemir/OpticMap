package com.elacqua.opticmap.ui.places

import android.content.Context
import androidx.lifecycle.*
import com.elacqua.opticmap.data.LocalRepository
import com.elacqua.opticmap.data.local.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlacesViewModel(private val localRepository: LocalRepository) : ViewModel() {

    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places

    fun getPlaces(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = localRepository.getAllPlaces()
            for (place in result) {
                val adr = localRepository.getAddress(context, place.latitude, place.longitude)
                if (adr != null) {
                    place.address = adr
                }
            }
            _places.postValue(result)
        }
    }

    fun deletePlace(place: Place) {
        viewModelScope.launch(Dispatchers.IO) {
            localRepository.deletePlaces(place)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class PlacesViewModelFactory(private val localRepository: LocalRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PlacesViewModel(localRepository) as T
    }
}
package com.elacqua.opticmap.ui.places

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elacqua.opticmap.R
import com.elacqua.opticmap.data.local.Place
import com.elacqua.opticmap.util.byteArrayToBitmap
import com.elacqua.opticmap.util.getDateFromEpoch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlaceRecyclerView(private val placeClickListener: PlaceClickListener) :
    RecyclerView.Adapter<PlaceRecyclerView.ViewHolder>() {

    private val places = ArrayList<Place>()

    fun setPlaces(newPlaces: List<Place>) {
        places.clear()
        places.addAll(newPlaces)
        notifyDataSetChanged()
    }

    fun removePlace(position: Int) {
        val removedPlace = places.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getItemAt(position: Int) = places[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.places_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
        holder.onClick(position)
    }

    override fun getItemCount() = places.size

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val txtPlaceName: TextView = view.findViewById(R.id.txt_place_name)
        private val txtPlaceLocation: TextView = view.findViewById(R.id.txt_place_location)
        private val txtPlaceDate: TextView = view.findViewById(R.id.txt_place_date)
        private val imgPlaceOcr: ImageView = view.findViewById(R.id.img_place_ocr)

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {
            txtPlaceName.text = places[position].name
            txtPlaceLocation.text =
                "${places[position].address.city} / ${places[position].address.country}"
            txtPlaceDate.text = getDateFromEpoch(places[position].date)
            if (places[position].image != null) {
                imgPlaceOcr.setImageBitmap(byteArrayToBitmap(places[position].image!!))
            }
        }

        fun onClick(position: Int) {
            view.setOnClickListener {
                placeClickListener.onPlaceClick(places[position])
            }
        }
    }
}
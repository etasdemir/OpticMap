package com.elacqua.opticmap.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elacqua.opticmap.data.local.Place
import com.elacqua.opticmap.databinding.FragmentPlaceBinding
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.byteArrayToBitmap
import com.elacqua.opticmap.util.getDateFromEpoch

class PlaceFragment : Fragment() {

    private var binding: FragmentPlaceBinding? = null
    private var place: Place? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgs()
        setViews()
    }

    private fun getArgs() {
        place = arguments?.get(Constant.PLACE_ARG_KEY) as Place
    }

    @SuppressLint("SetTextI18n")
    private fun setViews() {
        if (place == null) {
            return
        }
        binding!!.txtPlaceFragmentName.text = place!!.name
        binding!!.txtPlaceFragmentAddress.text =
            "${place!!.address.address} ${place!!.address.city} / ${place!!.address.country}"
        val date = getDateFromEpoch(place!!.date)
        binding!!.txtPlaceFragmentDate.text = date
        if (place!!.image != null) {
            val bitmap = byteArrayToBitmap(place!!.image!!)
            binding!!.imgPlaceFragment.setImageBitmap(bitmap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaceBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

}
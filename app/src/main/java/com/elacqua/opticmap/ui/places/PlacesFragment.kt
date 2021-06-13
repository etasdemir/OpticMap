package com.elacqua.opticmap.ui.places

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elacqua.opticmap.R
import com.elacqua.opticmap.data.LocalRepository
import com.elacqua.opticmap.data.local.Place
import com.elacqua.opticmap.data.local.PlacesDatabase
import com.elacqua.opticmap.databinding.FragmentPlacesBinding
import com.elacqua.opticmap.util.Constant

class PlacesFragment : Fragment() {

    private var binding: FragmentPlacesBinding? = null
    private lateinit var placesDatabase: PlacesDatabase
    private lateinit var placeRecyclerView: PlaceRecyclerView
    private lateinit var placesViewModel: PlacesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placesDatabase = PlacesDatabase.getInstance(requireContext())
        placesViewModel = ViewModelProvider(
            this,
            PlacesViewModelFactory(LocalRepository(placesDatabase.getPlacesDao()))
        ).get(PlacesViewModel::class.java)
        initRecyclerView()
        observePlacesData()
        placesViewModel.getPlaces(requireContext())
    }

    private fun observePlacesData() {
        placesViewModel.places.observe(viewLifecycleOwner, {
            placeRecyclerView.setPlaces(it)
        })
    }

    private fun initRecyclerView() {
        placeRecyclerView = PlaceRecyclerView(object : PlaceClickListener {
            override fun onPlaceClick(place: Place) {
                val args = bundleOf(Constant.PLACE_ARG_KEY to place)
                findNavController().navigate(
                    R.id.action_navigation_places_to_placeFragment,
                    args
                )
            }
        })
        binding!!.rvPlaces.run {
            adapter = placeRecyclerView
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL, false
            )
            setHasFixedSize(true)
        }

        val swipe = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                placesViewModel.deletePlace(placeRecyclerView.getItemAt(viewHolder.adapterPosition))
                placeRecyclerView.removePlace(viewHolder.adapterPosition)
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(binding!!.rvPlaces)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlacesBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}
package com.elacqua.opticmap.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.FragmentPhotoEditBinding
import com.elacqua.opticmap.util.Constant

class PhotoEditFragment : Fragment() {

    private var binding: FragmentPhotoEditBinding? = null
    private var picture: Bitmap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgs()
        handleEditComplete()
    }

    private fun getArgs() {
        arguments?.let { bundle ->
            picture = bundle.get(Constant.PHOTO_EDIT_KEY) as Bitmap
        }
        picture?.let { photo ->
            binding?.imgEditPhoto?.setImageBitmap(photo)
        }
    }

    private fun handleEditComplete() {
        binding?.btnEditComplete?.setOnClickListener {
            val args = bundleOf(Constant.OCR_IMAGE_KEY to picture)
            findNavController().navigate(R.id.action_photoEditFragment_to_ocrFragment, args)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoEditBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
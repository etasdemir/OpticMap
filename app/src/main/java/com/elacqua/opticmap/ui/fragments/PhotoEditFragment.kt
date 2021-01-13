package com.elacqua.opticmap.ui.fragments

import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elacqua.opticmap.databinding.FragmentPhotoEditBinding
import com.elacqua.opticmap.util.Constant

class PhotoEditFragment : Fragment() {

    private var binding: FragmentPhotoEditBinding? = null
    private var picture: Bitmap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgs()
    }

    private fun getArgs() {
        arguments?.let { bundle ->
            picture = bundle.get(Constant.PHOTO_EDIT_KEY) as Bitmap
        }
        picture?.let { photo ->
            binding?.imgEditPhoto?.setImageBitmap(photo)
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
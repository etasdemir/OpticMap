package com.elacqua.opticmap.ui.home

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.FragmentHomeBinding
import com.elacqua.opticmap.util.Constant
import kotlinx.coroutines.NonCancellable.cancel

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var binding: FragmentHomeBinding ?= null
    private var langFrom = "eng"
    private var langTo = "eng"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.btnLanguageFrom?.setOnClickListener {
            createLanguageDialog()
        }
    }

    private fun createLanguageDialog() {
        var selection = ""
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.home_dialog_title)
            .setSingleChoiceItems(Constant.languages, 0
            ) { _, selectedIndex ->
                selection = Constant.shortLang[selectedIndex]
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
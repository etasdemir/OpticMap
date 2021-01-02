package com.elacqua.opticmap.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.FragmentHomeBinding
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.Language
import com.elacqua.opticmap.util.TrainedDataDownloader
import timber.log.Timber

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var binding: FragmentHomeBinding? = null
    private var langFrom = "eng"
    private var langTo = "eng"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleLanguageButtons()

    }

    private fun handleLanguageButtons() {
        binding?.btnLanguageFrom?.setOnClickListener {
            createLanguageDialog(Language.FROM)
        }
        binding?.btnLanguageTo?.setOnClickListener {
            createLanguageDialog(Language.TO)
        }
    }

    private fun createLanguageDialog(type: Language) {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setTitle(R.string.home_dialog_title)
            setSingleChoiceItems(
                Constant.languages, 1
            ) { _, selectedIndex ->
                if (type == Language.FROM) {
                    langFrom = Constant.shortLang[selectedIndex]
                    downloadSelectedLanguage()
                } else {
                    langTo = Constant.shortLang[selectedIndex]
                }
            }
            create()
        }
    }

    private fun downloadSelectedLanguage() {
        val downloader = TrainedDataDownloader()
        downloader.download(requireContext(), langFrom)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
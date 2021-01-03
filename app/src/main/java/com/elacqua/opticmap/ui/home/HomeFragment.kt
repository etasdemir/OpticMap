package com.elacqua.opticmap.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.FragmentHomeBinding
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.Language
import com.elacqua.opticmap.ocr.TrainedDataDownloader
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var binding: FragmentHomeBinding? = null
    private var langFrom = "eng"
    private var langTo = "eng"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleLanguageButtons()
        handleGalleryAccess()

    }

    private fun handleGalleryAccess() {
        binding?.btnHomeGallery?.setOnClickListener {
            takeImageFromGallery()
        }
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
        builder.run {
            setTitle(R.string.home_dialog_title)
            setSingleChoiceItems(Constant.languages, -1) { dialog, selectedIndex ->
                if (type == Language.FROM) {
                    langFrom = Constant.shortLang[selectedIndex]
                    binding?.btnLanguageFrom?.text=Constant.languages[selectedIndex]
                    downloadTrainedData()
                } else {
                    langTo = Constant.shortLang[selectedIndex]
                    binding?.btnLanguageTo?.text=Constant.languages[selectedIndex]
                }
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun downloadTrainedData() {
        val isConnected = checkInternetStatus()
        if (!isConnected) {
            showNoInternetSnackbar()
            return
        }

        val downloader = TrainedDataDownloader()
        val isFileExist = downloader.isFileExist(requireContext(), langFrom)
        if (isFileExist) {
            Timber.d("Trained data file for selected language is exist")
            return
        }
        createDownloadDialog(downloader)
    }

    private fun createDownloadDialog(downloader: TrainedDataDownloader) {
        val builder = AlertDialog.Builder(requireContext())
        builder.run {
            setMessage(R.string.home_download_dialog_message)
            setTitle(R.string.home_download_dialog_title)
            setPositiveButton(R.string.home_download_dialog_accept) { dialog, _ ->
                downloader.download(requireContext(), langFrom)
                dialog.dismiss()
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    @Suppress("DEPRECATION")
    private fun checkInternetStatus(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            return activeNetwork?.isConnectedOrConnecting ?: false
        }
    }

    private fun showNoInternetSnackbar() {
        Snackbar.make(
            binding!!.relativeLayoutHome,
            R.string.home_no_internet,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.home_open_settings) {
                openSettings()
            }
            .show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivity(intent)
    }

    private fun takeImageFromGallery() {
        val photoPickIntent = Intent(Intent.ACTION_PICK)
        photoPickIntent.type = "image/*"
        startActivityForResult(photoPickIntent, Constant.IMAGE_PICK_INTENT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == Constant.IMAGE_PICK_INTENT_CODE) {
            try {
                val imageUri: Uri? = data?.data
                imageUri?.let { uri ->
                    val imageStream = requireActivity().contentResolver.openInputStream(uri)
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
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
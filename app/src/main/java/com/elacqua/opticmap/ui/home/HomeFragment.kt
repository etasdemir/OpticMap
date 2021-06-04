package com.elacqua.opticmap.ui.home

import android.Manifest
import android.R.attr.bitmap
import android.R.attr.name
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.FragmentHomeBinding
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.Language
import com.elacqua.opticmap.util.Languages
import com.elacqua.opticmap.util.SharedPref
import com.google.android.material.snackbar.Snackbar
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var binding: FragmentHomeBinding? = null
    private var langFrom = Constant.DEFAULT_LANGUAGE.shortName
    private var langTo = Constant.DEFAULT_LANGUAGE.shortName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!checkInternetStatus()) {
            showNoInternetSnackbar()
        }
        setLanguages()
        requestCameraPermission()
        binding?.run {
            btnLanguageFrom.setOnClickListener {
                createLanguageDialog(Language.FROM)
            }
            btnLanguageTo.setOnClickListener {
                createLanguageDialog(Language.TO)
            }
            btnHomeGallery.setOnClickListener {
                takeImageFromGallery()
            }
        }
    }

    private fun setCameraView() {
        binding?.run {
            camera.apply {
                setLifecycleOwner(viewLifecycleOwner)
                addCameraListener(object : CameraListener() {
                    override fun onPictureTaken(result: PictureResult) {
                        result.toBitmap { picture ->
                            picture?.let { image ->
                                if (isLanguageSelected()) {
                                    val imageUri = saveImgToCache(image)
                                    navigateToUCrop(imageUri)
                                }
                            }
                        }
                    }
                })
            }
            btnTakePicture.setOnClickListener {
                camera.takePicture()
            }
        }
    }

    private fun saveImgToCache(image: Bitmap): Uri {
        val outputDir = requireContext().cacheDir
        val outputFile = File.createTempFile(
            System.currentTimeMillis().toString(),
            ".jpeg",
            outputDir
        )
        val stream = FileOutputStream(outputFile.absolutePath)
        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        return Uri.fromFile(outputFile)
    }

    private fun navigateToUCrop(imageUri: Uri) {
        val outputDir = requireContext().cacheDir
        val outputFile = File.createTempFile(
            System.currentTimeMillis().toString(),
            ".jpeg",
            outputDir
        )

        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)
            setCompressionQuality(100)
            setHideBottomControls(false)
        }
        UCrop.of(imageUri, Uri.fromFile(outputFile))
            .withOptions(options)
            .start(requireActivity())
    }

    private fun takeImageFromGallery() {
        if (!isLanguageSelected()) {
            return
        }
        val photoPickIntent = Intent(Intent.ACTION_PICK)
        photoPickIntent.type = "image/*"
        startActivityForResult(photoPickIntent, Constant.IMAGE_PICK_INTENT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != AppCompatActivity.RESULT_OK) {
            return
        }
        when(requestCode) {
            Constant.IMAGE_PICK_INTENT_CODE -> {
                try {
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        navigateToUCrop(imageUri)
                    } else {
                        Timber.e("Gallery image uri is null")
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    private fun isLanguageSelected(): Boolean {
        return if (binding?.btnLanguageFrom?.text == getString(R.string.home_button_from) ||
            binding?.btnLanguageTo?.text == getString(R.string.home_button_to)
        ) {
            Toast.makeText(requireContext(), R.string.home_select_language, Toast.LENGTH_SHORT)
                .show()
            false
        } else {
            true
        }
    }

    private fun createLanguageDialog(type: Language) {
        val builder = AlertDialog.Builder(requireContext())
        builder.run {
            setTitle(R.string.home_dialog_title)
            setSingleChoiceItems(Languages.availableLanguages(), -1) { dialog, selectedIndex ->
                val pref = SharedPref(requireContext())
                if (type == Language.FROM) {
                    langFrom = Languages.values()[selectedIndex].shortName
                    pref.langFrom = langFrom
                    binding?.btnLanguageFrom?.text = Languages.getLanguageFromShortName(langFrom).name
                } else {
                    langTo = Languages.values()[selectedIndex].shortName
                    pref.langTo = langTo
                    binding?.btnLanguageTo?.text = Languages.getLanguageFromShortName(langTo).name
                }
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

    private fun setLanguages() {
        val pref = SharedPref(requireContext())
        langFrom = pref.langFrom
        langTo = pref.langTo
        binding?.btnLanguageFrom?.text = Languages.getLanguageFromShortName(langFrom).name
        binding?.btnLanguageTo?.text = Languages.getLanguageFromShortName(langTo).name
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                Constant.CAMERA_REQUEST_CODE
            )
        } else {
            setCameraView()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constant.CAMERA_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            setCameraView()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        removeStatusBar()
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @Suppress("DEPRECATION")
    private fun removeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
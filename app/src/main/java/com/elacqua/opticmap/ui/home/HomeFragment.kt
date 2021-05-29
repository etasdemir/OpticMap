package com.elacqua.opticmap.ui.home

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.FragmentHomeBinding
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.Language
import com.elacqua.opticmap.util.SharedPref
import com.google.android.material.snackbar.Snackbar
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var binding: FragmentHomeBinding? = null
    private var langFrom = Constant.DEFAULT_LANGUAGE
    private var langTo = Constant.DEFAULT_LANGUAGE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!checkInternetStatus()) {
            showNoInternetSnackbar()
            return
        }
        requestCameraPermission()
        setCameraView()
        setLanguages()
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
            btnTakePicture.setOnClickListener {
                camera.takePicture()
            }
            camera.run {
                setLifecycleOwner(viewLifecycleOwner)
                addCameraListener(object : CameraListener() {
                    override fun onPictureTaken(result: PictureResult) {
                        result.toBitmap { picture ->
                            picture?.let { image ->
                                if (!isLanguageSelected()) {
                                    return@toBitmap
                                }
                                saveMediaToStorage(image)
                                navigateToPhotoEditFragment(image)
                            }
                        }
                    }
                })
            }
        }
    }

    @Suppress("DEPRECATION")
    fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context?.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    private fun navigateToPhotoEditFragment(image: Bitmap) {
        val args = bundleOf(Constant.PHOTO_EDIT_KEY to image)
        findNavController().navigate(R.id.action_navigation_home_to_photoEditFragment, args)
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
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == Constant.IMAGE_PICK_INTENT_CODE) {
            try {
                val imageUri: Uri? = data?.data
                imageUri?.let { uri ->
                    val imageStream = requireActivity().contentResolver.openInputStream(uri)
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    navigateToOcrFragment(selectedImage)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun navigateToOcrFragment(image: Bitmap) {
        val args = bundleOf(Constant.OCR_IMAGE_KEY to image)
        findNavController().navigate(R.id.action_navigation_home_to_ocrFragment, args)
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
            setSingleChoiceItems(Constant.languages, -1) { dialog, selectedIndex ->
                val pref = SharedPref(requireContext())
                if (type == Language.FROM) {
                    langFrom = Constant.shortLang[selectedIndex]
                    pref.langFrom = langFrom
                    binding?.btnLanguageFrom?.text = Constant.languages[selectedIndex]
                } else {
                    langTo = Constant.shortLang[selectedIndex]
                    pref.langTo = langTo
                    binding?.btnLanguageTo?.text = Constant.languages[selectedIndex]
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
        binding?.btnLanguageFrom?.text = langFrom
        binding?.btnLanguageTo?.text = langTo
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                Constant.CAMERA_REQUEST_CODE
            )
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

            Timber.e("permission granted")
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
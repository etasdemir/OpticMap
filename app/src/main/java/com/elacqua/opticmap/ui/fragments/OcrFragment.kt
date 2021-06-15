package com.elacqua.opticmap.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.elacqua.opticmap.R
import com.elacqua.opticmap.data.LocalRepository
import com.elacqua.opticmap.data.local.Place
import com.elacqua.opticmap.data.local.PlacesDatabase
import com.elacqua.opticmap.databinding.DialogSaveOcrBinding
import com.elacqua.opticmap.databinding.FragmentOcrBinding
import com.elacqua.opticmap.ocr.*
import com.elacqua.opticmap.util.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.text.Text
import timber.log.Timber
import java.io.File
import java.util.*

class OcrFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var placesDatabase: PlacesDatabase
    private lateinit var ocrViewModel: OcrViewModel
    private lateinit var sharedPref: SharedPref
    private lateinit var tts: TextToSpeech
    private lateinit var ocr: MLKitOCRHandler
    private var binding: FragmentOcrBinding? = null
    private var imageUri: Uri? = null
    private var langFrom: Languages = Constant.DEFAULT_LANGUAGE
    private var langTo: Languages = Constant.DEFAULT_LANGUAGE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placesDatabase = PlacesDatabase.getInstance(requireContext())
        ocrViewModel = ViewModelProvider(
            this,
            OcrViewModelFactory(LocalRepository(placesDatabase.getPlacesDao()))
        ).get(OcrViewModel::class.java)
        getArgs()
        initOCR()
    }

    private fun initOCR() {
        UIState.isLoadingState.value = true
        tts = TextToSpeech(requireContext().applicationContext, this)
        val translator = MLTranslator(langFrom, langTo)
        ocr = MLKitOCRHandler(translator)
        translator.downloadModel { success ->
            if (success) {
                recognizeText()
            }
            UIState.isLoadingState.value = false
        }
    }

    private fun recognizeText() {
        UIState.isLoadingState.value = true
        val img = ocr.getImageFromUri(imageUri!!, requireContext())
        ocrViewModel.recognizeText(img, ocr)
        ocrViewModel.textsOnImage.observe(viewLifecycleOwner, { texts ->
            if (texts != null) {
                textToSpeech(texts)
                handleRadioButtons(texts)
            }
            UIState.isLoadingState.value = false
        })
    }

    private fun textToSpeech(texts: Text) {
        UIState.isLoadingState.value = true
        binding!!.btnOcrVoice.setOnClickListener {
            ocr.ocrToSpeech(texts, object : TranslateResultListener {
                override fun onSuccess(text: String) {
                    UIState.isLoadingState.value = false
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }

                override fun onFailure(message: String) {
                    UIState.isLoadingState.value = false
                }
            })
        }
    }

    private fun handleRadioButtons(text: Text) {
        binding!!.radioBtnOcrBlock.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked) {
                setLastRadioButton(text, RecognitionOptions.TRANSLATE_BLOCKS)
            }
            setRadioBtnTextColor(btn as RadioButton)
        }
        binding!!.radioBtnOcrLine.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked) {
                setLastRadioButton(text, RecognitionOptions.TRANSLATE_LINES)
            }
            setRadioBtnTextColor(btn as RadioButton)
        }
        binding!!.radioBtnOcrWhole.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked) {
                setLastRadioButton(text, RecognitionOptions.TRANSLATE_WHOLE)
            }
            setRadioBtnTextColor(btn as RadioButton)
        }
        when (sharedPref.lastSelectedRadioButton) {
            RecognitionOptions.TRANSLATE_BLOCKS.name -> {
                binding!!.radioBtnOcrBlock.isChecked = true
            }
            RecognitionOptions.TRANSLATE_LINES.name -> {
                binding!!.radioBtnOcrLine.isChecked = true
            }
            RecognitionOptions.TRANSLATE_WHOLE.name -> {
                binding!!.radioBtnOcrWhole.isChecked = true
            }
        }
    }

    private fun setLastRadioButton(text: Text, option: RecognitionOptions) {
        sharedPref.lastSelectedRadioButton = option.name
        getTextFromImage(text, option)
    }

    private fun setRadioBtnTextColor(btn: RadioButton) {
        if (btn.isChecked) {
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
        }
    }

    private fun getTextFromImage(text: Text, option: RecognitionOptions) {
        if (imageUri == null) {
            return
        }
        UIState.isLoadingState.value = true
        val bitmap = getBitmapFromUri(imageUri!!, requireContext().contentResolver)
        if (imageUri != null) {
            ocr.translateText(bitmap, text, option, object : OCRResultListener<Bitmap> {
                override fun onSuccess(result: Bitmap?) {
                    UIState.isLoadingState.value = false
                    if (result == null) {
                        binding?.imgOcrPicture?.setImageURI(imageUri)
                    } else {
                        binding?.imgOcrPicture?.setImageBitmap(result)
                        saveButtonHandler(result)
                    }
                }

                override fun onFailure(message: String) {
                    UIState.isLoadingState.value = false
                    Timber.e("OCR failed: $message")
                    binding?.imgOcrPicture?.setImageURI(imageUri)
                }
            })
        }
    }

    private fun saveButtonHandler(img: Bitmap) {
        binding!!.btnOcrSave.setOnClickListener {
            if (this.imageUri == null) {
                return@setOnClickListener
            }
            createSaveDialog(img)
        }
    }

    private fun createSaveDialog(img: Bitmap) {
        val dialog = Dialog(requireContext())
        dialog.run {
            val dialogBinding = DialogSaveOcrBinding.inflate(LayoutInflater.from(requireContext()))
            setContentView(dialogBinding.root)
            dialogBinding.txtPhotoName.requestFocus()
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialogBinding.btnDialogSave.setOnClickListener {
                val photoName = dialogBinding.txtPhotoName.text.toString()
                requestLocation { location ->
                    val savedUri = saveImageToGallery(img)
                    val lat = location.latitude
                    val long = location.longitude
                    val place = Place(0, photoName, lat, long, getEpochTime(), savedUri.toString())
                    ocrViewModel.savePlace(place)
                    Toast.makeText(requireContext(), R.string.ocr_image_saved, Toast.LENGTH_SHORT).show()
                }
                dismiss()
            }
            dialogBinding.btnDialogCancel.setOnClickListener {
                dismiss()
            }
            show()
        }
    }

    private fun requestLocation(callback: (location: Location) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            findLocation(callback)
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), Constant.LOCATION_PERM_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun findLocation(callback: (location: Location) -> Unit) {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            val fusedLocationFinder =
                LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationFinder.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    callback(location)
                } else {
                    val locationRequest = LocationRequest()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(5000)
                        .setFastestInterval(1000)
                        .setNumUpdates(1)
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult?) {
                            val loc = result?.lastLocation
                            loc?.let {
                                callback(loc)
                            }
                        }
                    }
                    fusedLocationFinder.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.myLooper()!!
                    )
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun saveImageToGallery(bitmap: Bitmap): Uri {
        val filename = "${System.currentTimeMillis()}.png"
        val url = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            filename,
            "OpticMap image"
        )
        return Uri.parse(url)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constant.LOCATION_PERM_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initOCR()
        }
    }

    private fun getArgs() {
        imageUri = arguments?.get(Constant.OCR_IMAGE_KEY) as Uri
        if (imageUri != null) {
            sharedPref = SharedPref(requireContext())
            langFrom = Languages.getLanguageFromShortName(sharedPref.langFrom)
            langTo = Languages.getLanguageFromShortName(sharedPref.langTo)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOcrBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onPause() {
        tts.stop()
        super.onPause()
    }

    override fun onDestroy() {
        UIState.isLoadingState.value = false
        ocr.closeTranslator()
        tts.shutdown()
        binding = null
        deleteFiles(requireContext().cacheDir.absolutePath, ".png")
        super.onDestroy()
    }

    private fun deleteFiles(dirPath: String, ext: String) {
        val dir = File(dirPath)
        if (!dir.exists()) return
        val fList: Array<File> = dir.listFiles()!!
        for (f in fList) {
            if (f.name.endsWith(ext)) {
                Timber.e("File deleted: ${f.name}")
                f.delete()
            }
        }
    }

    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }
}
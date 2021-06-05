package com.elacqua.opticmap.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.ActivityMainBinding
import com.elacqua.opticmap.util.Constant
import com.elacqua.opticmap.util.UIState
import com.elacqua.opticmap.util.getBitmapFromUri
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        observeUIState()
    }

    private fun observeUIState() {
        UIState.isLoadingState.observe(this, { isLoadingState ->
            if (isLoadingState) {
                binding.viewProgressMain.visibility = View.VISIBLE
                binding.progressBarMain.visibility = View.VISIBLE
            } else {
                binding.viewProgressMain.visibility = View.GONE
                binding.progressBarMain.visibility = View.GONE
            }
        })
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            UCrop.REQUEST_CROP -> {
                if (data != null) {
                    val resultUri = UCrop.getOutput(data)
                    val savedUri = saveImageToGallery(
                        getBitmapFromUri(
                            resultUri!!,
                            contentResolver
                        )
                    )
                    navigateToOcrFragment(savedUri!!)
                    deleteFiles(cacheDir.absolutePath, ".png")
                }
            }
            UCrop.RESULT_ERROR -> {
                if (data != null) {
                    Timber.e("onActivityResult: Crop error: ${UCrop.getError(data)}")
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val filename = "${System.currentTimeMillis()}.png"
        val url = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            filename,
            "OpticMap image"
        )
        return Uri.parse(url)
    }

    private fun navigateToOcrFragment(imageUri: Uri) {
        val args = bundleOf(Constant.OCR_IMAGE_KEY to imageUri)
        findNavController(R.id.nav_host_fragment).navigate(
            R.id.action_navigation_home_to_ocrFragment,
            args
        )
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
}
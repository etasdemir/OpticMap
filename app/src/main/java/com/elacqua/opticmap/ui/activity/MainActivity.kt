package com.elacqua.opticmap.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.elacqua.opticmap.R
import com.elacqua.opticmap.databinding.ActivityMainBinding
import com.elacqua.opticmap.util.UIState
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yalantis.ucrop.UCrop
import timber.log.Timber


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
}
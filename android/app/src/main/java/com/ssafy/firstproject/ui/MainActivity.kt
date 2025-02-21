package com.ssafy.firstproject.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseActivity
import com.ssafy.firstproject.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcv) as NavHostFragment
        navController = navHostFragment.navController
        binding.bnv.setupWithNavController(navController)

        hideBottomNavigationView(navController)

        binding.bnv.setOnItemSelectedListener { item ->
            if (item.itemId != binding.bnv.selectedItemId) {
                navController.navigate(item.itemId)
            }
            true
        }
    }

    private fun hideBottomNavigationView(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bnv.visibility = when (destination.id) {
                R.id.dest_home -> View.VISIBLE
                R.id.dest_game_start -> View.VISIBLE
                R.id.dest_check -> View.VISIBLE
                R.id.dest_search -> View.VISIBLE
                R.id.dest_my_page -> View.VISIBLE
                R.id.dest_image_result -> View.VISIBLE
                R.id.dest_logout_dialog -> View.VISIBLE
                else -> View.GONE
            }
        }
    }
}

//Navigational sections of MainActivity.kt completed with assistance
// from Microsoft Copilot as a 'learning assistant'
package com.example.GamrUI

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.GamrUI.fragments.ChatFragment
import com.example.GamrUI.fragments.ExploreFragment
import com.example.GamrUI.fragments.PeopleFragment
import com.example.GamrUI.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Load the default fragment first
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PeopleFragment())
            .commit()

        // Set listener for bottom navigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_people -> PeopleFragment()
                R.id.navigation_explore -> ExploreFragment()
                R.id.navigation_chat -> ChatFragment()
                R.id.navigation_profile -> ProfileFragment()
                else -> PeopleFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            true
        }
    }
}

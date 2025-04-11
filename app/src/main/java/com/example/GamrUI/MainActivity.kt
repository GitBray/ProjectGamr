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
        bottomNavigationView.setOnItemSelectedListener { item ->  // Changed this line
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_people -> selectedFragment = PeopleFragment()
                R.id.navigation_explore -> selectedFragment = ExploreFragment()
                R.id.navigation_chat -> selectedFragment = ChatFragment()
                R.id.navigation_profile -> selectedFragment = ProfileFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, selectedFragment)
                    .commit()
                true  // Return true to indicate that the selection was handled
            } else {
                false // Return false if no fragment was selected (shouldn't happen in this case)
            }
        }
        //Default page will be the 'people' algorithm page.
        bottomNavigationView.selectedItemId = R.id.navigation_people
    }
}
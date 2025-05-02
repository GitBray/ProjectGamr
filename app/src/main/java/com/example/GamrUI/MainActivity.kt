//Navigational sections of MainActivity.kt completed with assistance
// from Microsoft Copilot as a 'learning assistant'
package com.example.GamrUI

// import navigational Fragments
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.GamrUI.fragments.MatchListFragment
import com.example.GamrUI.fragments.ExploreFragment
import com.example.GamrUI.fragments.PeopleFragment
import com.example.GamrUI.fragments.ProfileFragment

class MainActivity : AppCompatActivity() { // the use of AppCompatActivity fixed crashes earlier in development
                                            // seems to help conflicts with android.material

    override fun onCreate(savedInstanceState: Bundle?) { // savedInstanceState uses Bundle to remember state of the activity
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) //initial layout

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation) // access the bottom nav bar from activity_main.xml

        // Transaction allows for fragments to be changed
        // Default fragment is PeopleFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PeopleFragment())
            .commit()

        // Bottom navigation listener - when a button is pressed, call it's relevant fragment.
        bottomNavigationView.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_people -> PeopleFragment()
                R.id.navigation_explore -> ExploreFragment()
                R.id.navigation_chat -> MatchListFragment()
                R.id.navigation_profile -> ProfileFragment()
                else -> PeopleFragment() // PeopleFragment is 'home page'
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            true
        }
    }
}

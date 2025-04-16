// LoginMainFragment.kt
package com.example.GamrUI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.GamrUI.R

// this is the landing fragment shown in the app after the app lanches
class LoginMainFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login_main, container, false)

        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val registerButton = view.findViewById<Button>(R.id.registerButton)

        // it navigates to loginfragment when login button is clicked
        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginMainFragment_to_loginFragment)
        }

        // it navigates to registerfragment when regester button is clicked
        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginMainFragment_to_registerFragment)
        }

        return view
    }
}

package com.example.gamr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginMainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_main, container, false)

        val loginButton: Button = view.findViewById(R.id.loginButton)
        val registerButton: Button = view.findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginMainFragment_to_loginFragment)
        }

        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginMainFragment_to_registerFragment)
        }

        return view
    }
}

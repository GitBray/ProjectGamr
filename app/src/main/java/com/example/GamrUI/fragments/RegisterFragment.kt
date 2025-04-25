package com.example.GamrUI.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.GamrUI.MainActivity
import com.example.GamrUI.R

// fragment to handle user regestration which collects the username, password, and email from user
class RegisterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val usernameField = view.findViewById<EditText>(R.id.usernameField)
        val emailField = view.findViewById<EditText>(R.id.emailField)
        val passwordField = view.findViewById<EditText>(R.id.passwordField)
        val registerButton = view.findViewById<Button>(R.id.registerButton)

        // when user clicks registerbutton
        registerButton.setOnClickListener {
            val username = usernameField.text.toString()

            if (username.isNotEmpty() && emailField.text.isNotEmpty() && passwordField.text.isNotEmpty()) {
                Toast.makeText(requireContext(), "Registered: $username", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}

package com.example.gamr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val usernameField: EditText = view.findViewById(R.id.usernameField)
        val emailField: EditText = view.findViewById(R.id.emailField)
        val passwordField: EditText = view.findViewById(R.id.passwordField)
        val registerButton: Button = view.findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val user = usernameField.text.toString()
            Toast.makeText(requireContext(), "Registered: $user", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}

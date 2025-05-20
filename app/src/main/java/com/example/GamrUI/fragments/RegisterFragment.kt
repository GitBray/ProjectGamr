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
import com.example.GamrUI.GenericResponse
import com.example.GamrUI.MainActivity
import com.example.GamrUI.R
import com.example.GamrUI.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val usernameField = view.findViewById<EditText>(R.id.usernameField)
        val passwordField = view.findViewById<EditText>(R.id.passwordField)
        val registerButton = view.findViewById<Button>(R.id.registerButton)

        // When register button is clicked
        registerButton.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            // call register API
            if (username.isNotEmpty() && password.isNotEmpty()) {
                RetrofitClient.apiService.registerUser(username, password)
                    .enqueue(object : Callback<GenericResponse> {
                        override fun onResponse(
                            call: Call<GenericResponse>,
                            response: Response<GenericResponse>
                        ) {
                            val res = response.body()
                            if (res?.status == "success") {
                                res.user_id?.let { userId ->
                                    val sharedPref = requireActivity().getSharedPreferences("GamrPrefs", android.content.Context.MODE_PRIVATE)
                                    sharedPref.edit().putInt("user_id", userId).apply()

                                    Toast.makeText(requireContext(), "Registered successfully!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(requireContext(), MainActivity::class.java))
                                    requireActivity().finish()
                                } ?: run {
                                    Toast.makeText(requireContext(), "User ID missing in response", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Registration failed: ${res?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                            Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}

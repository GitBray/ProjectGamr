package com.example.GamrUI.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.GamrUI.MainActivity
import com.example.GamrUI.R
import com.example.GamrUI.RetrofitClient
import com.example.GamrUI.GenericResponse
import me.pushy.sdk.Pushy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class LoginFragment : Fragment() {

    private val dummyUserId = 1 // Replace with actual user ID after real login system

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val emailField = view.findViewById<EditText>(R.id.emailField)
        val passwordField = view.findViewById<EditText>(R.id.passwordField)
        val loginButton = view.findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(requireContext(), "Logging in: $email", Toast.LENGTH_SHORT).show()

                // ✅ Start Pushy device registration
                thread {
                    try {
                        val deviceToken = Pushy.register(requireContext())
                        Log.d("Pushy", "Device Token: $deviceToken")

                        // ✅ Send token to backend
                        RetrofitClient.apiService.saveDeviceToken(dummyUserId, deviceToken)
                            .enqueue(object : Callback<GenericResponse> {
                                override fun onResponse(
                                    call: Call<GenericResponse>,
                                    response: Response<GenericResponse>
                                ) {
                                    Log.d("Pushy", "Device token saved successfully")
                                }

                                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                    Log.e("Pushy", "Failed to save token: ${t.message}")
                                }
                            })

                        // ✅ Go to main screen
                        requireActivity().runOnUiThread {
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            requireActivity().finish()
                        }

                    } catch (e: Exception) {
                        Log.e("Pushy", "Registration failed: ${e.message}", e)
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Pushy registration failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}

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
import retrofit2.Call
import com.example.GamrUI.RetrofitClient


// fragment to handle user login
class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val usernameField = view.findViewById<EditText>(R.id.emailField)
        val passwordField = view.findViewById<EditText>(R.id.passwordField)
        val loginButton = view.findViewById<Button>(R.id.loginButton)

        // when user clicks login
        loginButton.setOnClickListener {
            val email = usernameField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Call login API
                RetrofitClient.apiService.loginUser(email, password)
                    .enqueue(object : retrofit2.Callback<GenericResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<GenericResponse>,
                            response: retrofit2.Response<GenericResponse>
                        ) {
                            val res = response.body()
                            if (res?.status == "success") {
                                Toast.makeText(requireContext(), "Login success", Toast.LENGTH_SHORT).show()
                                res.user_id?.let { userId ->
                                    val sharedPref = requireActivity().getSharedPreferences("GamrPrefs", android.content.Context.MODE_PRIVATE)
                                    sharedPref.edit().putInt("user_id", userId).apply()
                                }


                                startActivity(Intent(requireContext(), MainActivity::class.java))
                                requireActivity().finish()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Login failed: ${res?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(
                            call: retrofit2.Call<GenericResponse>,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                requireContext(),
                                "Network error: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return view
    }
}

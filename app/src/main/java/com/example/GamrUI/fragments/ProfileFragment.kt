package com.example.GamrUI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.GamrUI.R
import com.example.GamrUI.RetrofitClient
import android.widget.*
import retrofit2.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.GamrUI.UserProfile
import com.example.GamrUI.GenericResponse




class ProfileFragment : Fragment() {

    private lateinit var editTextBio: EditText
    private lateinit var editTextDiscord: EditText
    private lateinit var editTextInstagram: EditText
    private lateinit var spinnerStyle: Spinner

    private val userId = 1 // Replace with real logged-in user ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        editTextBio = view.findViewById(R.id.editTextBio)
        editTextDiscord = view.findViewById(R.id.editTextDiscord)
        editTextInstagram = view.findViewById(R.id.editTextInstagram)
        spinnerStyle = view.findViewById(R.id.spinnerPlayingStyle)

        view.findViewById<Button>(R.id.buttonSave).setOnClickListener { saveProfile() }
        view.findViewById<Button>(R.id.buttonDiscard).setOnClickListener { loadProfile() }

        loadProfile()

        return view
    }

    private fun loadProfile() {
        RetrofitClient.apiService.getProfile(userId).enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                val profile = response.body() ?: return
                editTextBio.setText(profile.bio)
                editTextDiscord.setText(profile.discord)
                editTextInstagram.setText(profile.instagram)

                val styles = resources.getStringArray(R.array.playing_styles)
                val index = styles.indexOfFirst { it.equals(profile.playing_style, ignoreCase = true) }
                if (index >= 0) spinnerStyle.setSelection(index)
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfile() {
        val bio = editTextBio.text.toString()
        val discord = editTextDiscord.text.toString()
        val instagram = editTextInstagram.text.toString()
        val playStyle = spinnerStyle.selectedItem.toString()

        RetrofitClient.apiService.updateProfile(userId, bio, discord, instagram, playStyle)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    val res = response.body()
                    if (res?.status == "success") {
                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Update failed: ${res?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(context, "Error saving profile", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

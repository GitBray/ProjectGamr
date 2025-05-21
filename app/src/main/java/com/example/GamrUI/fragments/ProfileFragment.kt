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
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.bumptech.glide.Glide


class ProfileFragment : Fragment() {

    private lateinit var editTextBio: EditText
    private lateinit var editTextDiscord: EditText
    private lateinit var editTextInstagram: EditText
    private lateinit var spinnerStyle: Spinner

    private val PICK_IMAGE_REQUEST = 1001
    private var selectedImageUri: Uri? = null
    private lateinit var imageView: ImageView

    private fun getUserId(): Int{
        val sharedPref = requireActivity().getSharedPreferences("GamrPrefs", AppCompatActivity.MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        editTextBio = view.findViewById(R.id.editTextBio)
        editTextDiscord = view.findViewById(R.id.editTextDiscord)
        editTextInstagram = view.findViewById(R.id.editTextInstagram)
        spinnerStyle = view.findViewById(R.id.spinnerPlayingStyle)

        imageView = view.findViewById(R.id.imageViewProfile)
        val buttonSelectImage = view.findViewById<Button>(R.id.buttonSelectImage)

        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        view.findViewById<Button>(R.id.buttonSave).setOnClickListener { saveProfile() }
        view.findViewById<Button>(R.id.buttonDiscard).setOnClickListener { loadProfile() }

        loadProfile()

        return view
    }

    private fun saveProfile() {
        val userId = getUserId()
        if (userId == -1){
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val bio = editTextBio.text.toString()
        val discord = editTextDiscord.text.toString()
        val instagram = editTextInstagram.text.toString()
        val playStyle = spinnerStyle.selectedItem.toString()

        // Convert strings to RequestBody
        val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val bioBody = bio.toRequestBody("text/plain".toMediaTypeOrNull())
        val discordBody = discord.toRequestBody("text/plain".toMediaTypeOrNull())
        val instagramBody = instagram.toRequestBody("text/plain".toMediaTypeOrNull())
        val playStyleBody = playStyle.toRequestBody("text/plain".toMediaTypeOrNull())

        // Optional image part
        val imagePart = selectedImageUri?.let { uri ->
            val inputStream = requireContext().contentResolver.openInputStream(uri)!!
            val tempFile = File(requireContext().cacheDir, "upload.jpg")
            tempFile.outputStream().use { output -> inputStream.copyTo(output) }

            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
        }

        // Call Retrofit
        RetrofitClient.apiService.updateProfileWithImage(
            userIdBody,
            bioBody,
            discordBody,
            instagramBody,
            playStyleBody,
            imagePart  // can be null!
        ).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                val result = response.body()
                if (result?.status == "success") {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Update failed: ${result?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                imageView.setImageURI(it)
            }
        }
    }


    private fun loadProfile() {
        val userId = getUserId()
        if (userId == -1){
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.getProfile(userId).enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                val profile = response.body() ?: return
                editTextBio.setText(profile.bio)
                editTextDiscord.setText(profile.discord)
                editTextInstagram.setText(profile.instagram)

                val styles = resources.getStringArray(R.array.playing_styles)
                val index = styles.indexOfFirst { it.equals(profile.preferred_playstyle, ignoreCase = true) }
                if (index >= 0) spinnerStyle.setSelection(index)

                // Load image from server if available
                profile.image_url?.let { url ->
                    Glide.with(requireContext())
                        .load(url)
                        .placeholder(R.drawable.default_profile) // Optional
                        .into(imageView)
                }

                Log.d("PROFILE_DEBUG", response.body().toString())

            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

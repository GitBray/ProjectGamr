package com.example.GamrUI
import java.io.Serializable

// maps user profile pulled from database
data class User(
    val user_id: Int,
    val gamertag: String?,
    val name: String?,
    val age: Int?,
    val preferred_playstyle: String?,
    val current_game: String?,
    val bio: String?,
    val latitude: Double?,
    val longitude: Double?,
    val current_game_genre: String?,
    val image_url: String? = null
): Serializable
// the addition of serializable allows for the use of 'Bundle' to
// access this class in other fragments.

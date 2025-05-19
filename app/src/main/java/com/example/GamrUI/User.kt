package com.example.GamrUI
import java.io.Serializable

// maps user profile pulled from database
data class User(
    val user_id: Int,
    val gamertag: String,
    val name: String,
    val age: Int,
    val preferred_playstyle: String,
    val current_game: String,
    val current_game_genre: String,
    val bio: String,
    val latitude: Double?,
    val longitude: Double?,
    val image_url: String?
): Serializable
// the addition of serializable allows for the use of 'Bundle' to
// access this class in other fragments.

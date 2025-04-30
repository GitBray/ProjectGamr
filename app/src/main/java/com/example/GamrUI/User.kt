package com.example.GamrUI

// maps user profile pulled from database
data class User(
    val user_id: Int,
    val gamertag: String,
    val name: String,
    val age: Int,
    val preferred_playstyle: String,
    val current_game: String,
    val bio: String,
    val latitude: Double?,
    val longitude: Double?
)

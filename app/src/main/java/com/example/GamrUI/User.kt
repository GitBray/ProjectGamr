package com.example.GamrUI

data class User(
    val user_id: Int,
    val gamertag: String,
    val name: String,
    val age: Int,
    val preferred_playstyle: String,
    val current_game: String,
    val bio: String
)
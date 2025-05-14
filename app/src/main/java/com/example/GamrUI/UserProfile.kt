package com.example.GamrUI

import com.google.gson.annotations.SerializedName

data class UserProfile(
    val user_id: Int,
    val bio: String?,
    val discord: String?,
    val instagram: String?,
    @SerializedName("playing_style") val preferred_playstyle: String?
)

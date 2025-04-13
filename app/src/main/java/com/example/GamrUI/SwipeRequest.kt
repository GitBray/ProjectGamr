package com.example.GamrUI.api

data class SwipeRequest(
    val swiper_id: Int,
    val swipee_id: Int,
    val direction: String
)
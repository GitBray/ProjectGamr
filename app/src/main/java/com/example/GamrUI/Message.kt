package com.example.GamrUI

data class Message(
    val message_id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val message: String,
    val timestamp: String
)

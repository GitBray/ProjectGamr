package com.example.GamrUI

data class Message(
    val message_id: Int,
    val sender_id: Int,
    val reciever_id: Int, // any instance of 'receiver' has been turned to 'reciever' to match
                            // a typo in the sql database
    val message: String,
    val timestamp: String
)

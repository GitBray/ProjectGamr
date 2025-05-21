package com.example.GamrUI.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.example.GamrUI.*
import java.text.SimpleDateFormat
import java.util.*

class ChatWindowFragment : Fragment() {

    private lateinit var selectedUser: User
    private lateinit var chatMessages: LinearLayout

    companion object {
        private const val USER_KEY = "user_key" // user key is used for communicating the user
                                                // between matchlist and chat window

        fun newInstance(user: User): ChatWindowFragment {
            val fragment = ChatWindowFragment()
            val bundle = Bundle()
            bundle.putSerializable(USER_KEY, user)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedUser = requireArguments().getSerializable(USER_KEY) as User
    }
    // Inflate the UI w/ send button, input field, and message field
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_chat_window, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.chatToolbar)
        toolbar.title = "Chatting with: ${selectedUser.gamertag}" // toolbar fields are defined above scrollview in chat xml
        chatMessages = view.findViewById(R.id.chatMessages)

        val sendButton = view.findViewById<Button>(R.id.sendButton)
        val inputField = view.findViewById<EditText>(R.id.messageInput)

        // current userid is stored in GamrPrefs, grab the id from there.
        val sharedPref = requireActivity().getSharedPreferences("GamrPrefs", android.content.Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("user_id", -1)

        if (currentUserId == -1) { // make sure the current user has an id
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
        } else {

            // Load chat history from backend via get_messages.php
            RetrofitClient.apiService.getMessages(currentUserId, selectedUser.user_id)
                .enqueue(object : retrofit2.Callback<List<Message>> {
                    override fun onResponse(call: retrofit2.Call<List<Message>>, response: retrofit2.Response<List<Message>>) {
                        if (response.isSuccessful) {
                            val messages = response.body() ?: emptyList()
                            messages.forEach { message ->
                                val isUserMessage = message.sender_id == currentUserId
                                addMessage(message.message, isUserMessage, message.timestamp)
                            }
                        } else { // error output for logic issues
                            Toast.makeText(requireContext(), "Failed to load messages", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<List<Message>>, t: Throwable) { //error output for network issues
                        Toast.makeText(requireContext(), "Error loading messages: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Handle sending messages
        sendButton.setOnClickListener {
            val messageText = inputField.text.toString()
            if (messageText.isNotBlank()) {
                if (currentUserId == -1) {
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val timestamp = getCurrentTimestamp() // grab timestamp to send over

                RetrofitClient.apiService.sendMessage( // call send_message php, send over userid's contents, & timestamp
                    currentUserId,
                    selectedUser.user_id,
                    messageText,
                    timestamp
                ).enqueue(object : retrofit2.Callback<GenericResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<GenericResponse>,
                        response: retrofit2.Response<GenericResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            addMessage(messageText, true, timestamp)
                            inputField.text.clear()
                        } else { // logic errors
                            Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<GenericResponse>, t: Throwable) { // network issue
                        Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            }
        }

        return view
    }

    // display message of given fields from database
    private fun addMessage(messageText: String, isUserMessage: Boolean, timestamp: String) {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
                bottomMargin = 12
            }
            gravity = if (isUserMessage) Gravity.END else Gravity.START
        }

        val styledMessage = "$messageText\n$timestamp" // seperating message & timestamp make for easy timestamp formatting

        // text bubble display, received and current users use a seperate bubble asset
        // see xml
        val messageView = TextView(requireContext()).apply {
            text = styledMessage
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(24, 12, 24, 12)
            setBackgroundResource(if (isUserMessage) R.drawable.bubble_sent else R.drawable.bubble_received)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = if (isUserMessage) 64 else 8
                marginEnd = if (isUserMessage) 8 else 64
            }

            post {
                val fullText = this.text.toString()
                val spanStart = fullText.lastIndexOf('\n') + 1
                val spanEnd = fullText.length
                val spannable = android.text.SpannableString(fullText)
                spannable.setSpan(
                    android.text.style.RelativeSizeSpan(0.65f),
                    spanStart,
                    spanEnd,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setText(spannable)
            }
        }

        container.addView(messageView)
        chatMessages.addView(container)

    }

    // Grabs current timestamp to store and display
    private fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

}

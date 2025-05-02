package com.example.GamrUI.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.example.GamrUI.Message
import com.example.GamrUI.R
import com.example.GamrUI.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatWindowFragment : Fragment() {

    private lateinit var selectedUser: User
    private lateinit var chatMessages: LinearLayout

    companion object {

        private const val USER_KEY = "user_key"

        // passes a selected user object into the fragment using 'Bundle' and 'Serializable'
        fun newInstance(user: User): ChatWindowFragment {
            val fragment = ChatWindowFragment()
            val bundle = Bundle()
            bundle.putSerializable(USER_KEY, user)
            fragment.arguments = bundle
            return fragment
        }
    }

    // gets the selected user from the bundle when fragment is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedUser = requireArguments().getSerializable(USER_KEY) as User
    }

    // chat window UI is designed in fragment_chat_window.xml
    // inflate the XML when fragment is created
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_chat_window, container, false)
        chatMessages = view.findViewById(R.id.chatMessages)

        val sendButton = view.findViewById<Button>(R.id.sendButton)
        val inputField = view.findViewById<EditText>(R.id.messageInput)

        // Dummy chat history
        // Pulls from the database will be implemented at a later time.
        val dummyMessages = listOf(
            Message(1, selectedUser.user_id, 1, "Wanna play?", "2024-05-01"),
            Message(2, 1, selectedUser.user_id, "Sure", "2024-05-01")
        )

        // Pass dummy messages to addMessage()
        dummyMessages.forEach { message ->
            val isUserMessage = message.sender_id == 1
            addMessage(message.message, isUserMessage, message.timestamp)
        }

        // Send button functionality
        // Text field and a timestamp is sent over to addMessage()
        // input field is then cleared
        sendButton.setOnClickListener {
            val message = inputField.text.toString()
            if (message.isNotBlank()) {
                addMessage(message, true, getCurrentTimestamp())
                inputField.text.clear()
            }
        }

        return view
    }

    // addMessage adds a sent message to the chat window
    // inserts a linear layout below previous messages, and has a textview for message contents
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

            gravity = if (isUserMessage) Gravity.END else Gravity.START // aligns message left or right
        }

        val styledMessage = "$messageText\n$timestamp" // makes message and timestamp 1 string

        // text view parameters
        // chat bubbles have seperate drawables for 'sent' bubbles and 'received' bubbles
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

            // shrink the timestamp part to 65% of text size.
            post {
                val fullText = this.text.toString()
                val spanStart = fullText.lastIndexOf('\n') + 1 // \n marks end of message, and beginning of timestamp
                val spanEnd = fullText.length
                val spannable = android.text.SpannableString(fullText)
                spannable.setSpan(
                    android.text.style.RelativeSizeSpan(0.65f), // set timestamp size to 65%
                    spanStart,
                    spanEnd,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setText(spannable) // add timestamp to the text view
            }
        }
        container.addView(messageView) // adds message text to linear layout container
        chatMessages.addView(container) // adds container to the chat window
    }

    // grabs current time and formats it in HH:mm format.
    // relative time will be implemented later
    private fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

}

package com.example.GamrUI.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.GamrUI.ui.theme.GamrUITheme
import com.example.GamrUI.RetrofitClient
import com.example.GamrUI.User
import kotlinx.coroutines.launch

// This is the local UI model for class LocalUser
data class LocalUser(
    val userId: Int,
    val gamertag: String,
    val name: String,
    val age: Int,
    val preferredPlaystyle: String,
    val currentGame: String,
    val bio: String
)

// Stores a user's choice on another user
data class Swipe(
    val swiperId: Int,
    val swipeeId: Int,
    val direction: String,
    val timestamp: Long = System.currentTimeMillis()
)

class PeopleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply { //Creates a view that applies the theme and
            setContent {                             // calls the method for making the Profile Feed Screen
                GamrUITheme {
                    ProfileFeedScreen()
                }
            }
        }
    }

    @Composable
    fun ProfileFeedScreen() {
        val currentUserId = 1 //sets user to TheRealBatman
        val swipeHistory = remember { mutableStateListOf<Swipe>() }
        var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
        val coroutineScope = rememberCoroutineScope()

        // Fetch data from backend when screen starts
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    val response = RetrofitClient.apiService.getUserFeed(currentUserId)
                    if (response.isSuccessful) {
                        allUsers = response.body() ?: emptyList()
                        Log.d("API", "Fetched ${allUsers.size} users")
                    } else {
                        Log.e("API", "Response error: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("API", "Exception: ${e.message}")
                }
            }
        }

        // Handles user's choice on another user and sends it to the server
        fun handleSwipe(swipee: User, direction: String) {
            val swipe = Swipe(
                swiperId = currentUserId,
                swipeeId = swipee.user_id,
                direction = direction
            )
            swipeHistory.add(swipe)

            Log.d("SWIPE_TRACK", "Swiped $direction on ${swipee.gamertag}")

            coroutineScope.launch {
                try {
                    val response = RetrofitClient.apiService.submitSwipe(
                        swiperId = currentUserId,
                        swipeeId = swipee.user_id,
                        direction = direction
                    )
                    if (response.isSuccessful && response.body()?.get("success") == true) {
                        Log.d("API", "Swipe sent successfully!")
                    } else {
                        Log.e("API", "Swipe failed: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("API", "Swipe error: ${e.message}")
                }
            }
        }

        // filters out already swiped users
        val recommendedUsers = allUsers.filter { user ->
            swipeHistory.none { it.swipeeId == user.user_id && it.swiperId == currentUserId }
        }

        // displays first recommended user with swipe card
        if (recommendedUsers.isNotEmpty()) {
            val user = recommendedUsers.first()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween // Space between card and buttons
            ) {
                // Swipeable card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // takes up available space
                    contentAlignment = Alignment.Center
                ) {
                    SwipeableProfileCard(
                        user = user.toLocalUser(),
                        onSwipe = { direction -> handleSwipe(user, direction) }
                    )
                }

                // Like and Dislike buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom=16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { handleSwipe(user, "dislike") }) {
                        Text("Dislike")
                    }
                    Button(onClick = { handleSwipe(user, "like") }) {
                        Text("Like")
                    }
                }
            }
        } else {
            Text("No more users to display", style = MaterialTheme.typography.bodyLarge)
        }
    }

    @Composable
    fun SwipeableProfileCard(user: LocalUser, onSwipe: (String) -> Unit) {
        var offsetX by remember { mutableStateOf(0f) }
        val animatableOffsetX = remember { Animatable(0f)}

        LaunchedEffect(offsetX) {
            if (offsetX > 100f) {
                animatableOffsetX.animateTo(500f)  // animates the card on a right swipe
                onSwipe("like")  // performs like action
                animatableOffsetX.snapTo(0f)  // resets card for next swipe
                offsetX = 0f
            } else if (offsetX < -100f) {
                animatableOffsetX.animateTo(-500f) // animates the card on a left swipe
                onSwipe("dislike")  // performs dislike action
                animatableOffsetX.snapTo(0f)  // resets card for next swipe
                offsetX = 0f
            }
        }

        Box( // card is a center aligned box
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { // pointerInput handles reading of swipe input
                    detectHorizontalDragGestures { _, dragAmount ->
                        offsetX += dragAmount // communicate severity of swipe
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            ProfileCard( // displays the card information within the swipable card
                user = user,
                modifier = Modifier.offset { IntOffset(animatableOffsetX.value.toInt(), 0) }
            )
        }
    }

    @Composable
    fun ProfileCard(user: LocalUser, modifier: Modifier = Modifier) { // handles information stored
        Card(
            modifier = modifier
                .fillMaxWidth(0.9f) // takes up 90% of screen width
                .height(500.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = user.gamertag, style = MaterialTheme.typography.titleLarge)
                Text(text = "Name: ${user.name}")
                Text(text = "Age: ${user.age}")
                Text(text = "Style: ${user.preferredPlaystyle}")
                Text(text = "Game: ${user.currentGame}")
                Text(text = "Bio: ${user.bio}")
            }
        }
    }

    // converts backend User model to UI model
    fun User.toLocalUser(): LocalUser {
        return LocalUser(
            userId = user_id,
            gamertag = gamertag,
            name = name,
            age = age,
            preferredPlaystyle = preferred_playstyle,
            currentGame = current_game,
            bio = bio
        )
    }
}

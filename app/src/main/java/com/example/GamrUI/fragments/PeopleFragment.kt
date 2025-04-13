package com.example.GamrUI.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.GamrUI.ui.theme.GamrUITheme
import com.example.GamrUI.RetrofitClient
import com.example.GamrUI.User
import kotlinx.coroutines.launch

// ✅ This is your local UI model
data class LocalUser(
    val userId: Int,
    val gamertag: String,
    val name: String,
    val age: Int,
    val preferredPlaystyle: String,
    val currentGame: String,
    val bio: String
)

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
        return ComposeView(requireContext()).apply {
            setContent {
                GamrUITheme {
                    ProfileFeedScreen()
                }
            }
        }
    }

    @Composable
    fun ProfileFeedScreen() {
        val currentUserId = 1
        val swipeHistory = remember { mutableStateListOf<Swipe>() }
        var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
        val coroutineScope = rememberCoroutineScope()

        // ✅ Fetch from backend
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



        val recommendedUsers = allUsers.filter { user ->
            swipeHistory.none { it.swipeeId == user.user_id && it.swiperId == currentUserId }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(recommendedUsers) { user ->
                ProfileCard(
                    user = user.toLocalUser(),
                    onSwipe = { direction -> handleSwipe(user, direction) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    @Composable
    fun ProfileCard(user: LocalUser, onSwipe: (String) -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = user.gamertag, style = MaterialTheme.typography.titleLarge)
                Text(text = "Name: ${user.name}")
                Text(text = "Age: ${user.age}")
                Text(text = "Style: ${user.preferredPlaystyle}")
                Text(text = "Game: ${user.currentGame}")
                Text(text = "Bio: ${user.bio}")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { onSwipe("dislike") }) {
                        Text("Dislike")
                    }
                    Button(onClick = { onSwipe("like") }) {
                        Text("Like")
                    }
                }
            }
        }
    }
}

// ✅ Converts backend user model to local UI model
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

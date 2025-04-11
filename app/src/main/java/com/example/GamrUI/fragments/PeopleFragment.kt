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

class PeopleFragment : Fragment() {

    data class User(
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

        val allUsers = listOf(
            User(1, "theRealBatman", "Gage", 21, "Casual", "Arkham Knight", "Justice for Gotham"),
            User(2, "BananaJoe", "Joe", 23, "Competitive", "Call of Duty: Black Ops 6", "Need a group."),
            User(3, "KillerQueen21", "Eva", 19, "Competitive", "Valorant", "Help me climb the ranks."),
            User(4, "UnitMan", "Luke", 27, "Casual", "Stardew Valley", "Let me show you my town."),
            User(5, "FireGamer69", "John", 21, "Casual", "Palworld", "Help me explore the world because I keep dying."),
            User(6, "GamingValkyrie", "Elizabeth", 20, "Competitive", "Valorant", "Looking to help climb the ranks."),
            User(7, "GokuMain420", "Dan", 24, "Competitive", "Dragon Ball Fighters", "Looking for strong opponents to fight."),
            User(8, "SakuraPetal333", "Mary", 22, "Casual", "Garry's Mod", "Looking for someone to make maps with."),
            User(9, "CapMerica", "Ethan", 21, "Competitive", "Marvel Rivals", "I need a group to play with."),
            User(10, "PinkPower34512", "Sarah", 18, "Competitive", "Super Smash Bros Ultimate", "I need help with combos, can someone please teach me.")
        )

        val swipeHistory = remember { mutableStateListOf<Swipe>() }

        fun handleSwipe(swipee: User, direction: String) {
            val swipe = Swipe(
                swiperId = currentUserId,
                swipeeId = swipee.userId,
                direction = direction
            )
            swipeHistory.add(swipe)
            Log.d("SWIPE_TRACK", "User $currentUserId swiped $direction on ${swipee.gamertag}")
        }

        val recommendedUsers = allUsers.filter { user ->
            swipeHistory.none { it.swipeeId == user.userId && it.swiperId == currentUserId }
                    && user.userId != currentUserId
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(recommendedUsers) { user ->
                ProfileCard(user = user, onSwipe = { direction ->
                    handleSwipe(user, direction)
                })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    @Composable
    fun ProfileCard(user: User, onSwipe: (String) -> Unit) {
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

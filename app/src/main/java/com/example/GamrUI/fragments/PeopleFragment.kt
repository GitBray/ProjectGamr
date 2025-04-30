package com.example.GamrUI.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
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
import android.content.Context
import android.location.Address
import java.util.*
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.GamrUI.ui.theme.GamrUITheme
import com.example.GamrUI.RetrofitClient
import com.example.GamrUI.User
import com.example.GamrUI.FilterViewModel
import kotlinx.coroutines.launch
import java.io.IOException

// This is the local UI model for class LocalUser
data class LocalUser(
    val userId: Int,
    val gamertag: String,
    val name: String,
    val age: Int,
    val preferredPlaystyle: String,
    val currentGame: String,
    val bio: String,
    val latitude: Double?, // Added latitude
    val longitude: Double?, // Added longitude
    val nearestTown: String?
)


// Stores a user's choice on another user
data class Swipe(
    val swiperId: Int,
    val swipeeId: Int,
    val direction: String,
    val timestamp: Long = System.currentTimeMillis()
)

class PeopleFragment : Fragment() {
    private val filterViewModel: FilterViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Fetch current location
        fetchUserLocation()
    }

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

    private fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLatitude = it.latitude
                currentLongitude = it.longitude
                Log.d("LOCATION", "Latitude: $currentLatitude, Longitude: $currentLongitude")
            } ?: Log.e("LOCATION", "Failed to fetch location.")
        }.addOnFailureListener {
            Log.e("LOCATION", "Error: ${it.message}")
        }
    }

    @Composable
    fun ProfileFeedScreen() {
        val currentUserId = 1 // Sets user to TheRealBatman
        val swipeHistory = remember { mutableStateListOf<Swipe>() }
        var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
        val coroutineScope = rememberCoroutineScope()

        val selectedPlaystyles by filterViewModel.selectedPlaystyles.collectAsState()

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

        // Filter out already swiped users
        val recommendedUsers = allUsers.filter { user ->
            swipeHistory.none { it.swipeeId == user.user_id && it.swiperId == currentUserId } &&
                    (selectedPlaystyles.isEmpty() || selectedPlaystyles.contains(user.preferred_playstyle))
        }

        if (recommendedUsers.isNotEmpty()) {
            val user = recommendedUsers.first()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Pass context to toLocalUser
                    SwipeableProfileCard(
                        user = user.toLocalUser(requireContext()),  // Pass context here
                        onSwipe = { direction -> handleSwipe(user, direction) }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
        val animatableOffsetX = remember { Animatable(0f) }

        LaunchedEffect(offsetX) {
            if (offsetX > 100f) {
                animatableOffsetX.animateTo(500f)
                onSwipe("like")
                animatableOffsetX.snapTo(0f)
                offsetX = 0f
            } else if (offsetX < -100f) {
                animatableOffsetX.animateTo(-500f)
                onSwipe("dislike")
                animatableOffsetX.snapTo(0f)
                offsetX = 0f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        offsetX += dragAmount
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            ProfileCard(
                user = user,
                modifier = Modifier.offset { IntOffset(animatableOffsetX.value.toInt(), 0) }
            )
        }
    }

    fun getNearestTown(context: Context, latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val city = addresses[0].locality ?: addresses[0].subAdminArea
                val state = addresses[0].adminArea
                if (city != null && state != null) "$city, $state"
                else city ?: state ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            "Unknown"
        }
    }

    @Composable
    fun ProfileCard(user: LocalUser, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.9f)
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
                Text(text = "City: ${user.nearestTown ?: "Unknown"}")
            }
        }
    }

    fun User.toLocalUser(context: Context): LocalUser {
        return LocalUser(
            userId = user_id,
            gamertag = gamertag,
            name = name,
            age = age,
            preferredPlaystyle = preferred_playstyle,
            currentGame = current_game,
            bio = bio,
            latitude = latitude,
            longitude = longitude,
            nearestTown = getNearestTown(requireContext(), latitude ?: 0.0, longitude ?: 0.0)
        )
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
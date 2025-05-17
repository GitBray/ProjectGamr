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
import android.widget.Toast
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
import kotlin.math.pow
import kotlin.math.roundToInt

// This is the local UI model for class LocalUser
data class LocalUser(
    val userId: Int,
    val gamertag: String,
    val name: String,
    val age: Int,
    val preferredPlaystyle: String,
    val currentGame: String,
    val bio: String,
    val latitude: Double?,
    val longitude: Double?,
    val nearestTown: String?,
    val distanceInMiles: Double? = null
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

    // Permission initialized in AndroidManifest, Permissions requested here.
    // Makes use of FusedLocationProviderClient to get current location
    // AI assisted with use of 'Context', it's purpose is to assist in accessing resources
    // (like location) from outside of the fragment.
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
            requestPermissions( //requestPermissions is a deprecated function and will be replaced
                                // app is still currently functional with it's inclusion.
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        //Error detection - Virtual Devices don't seem to like using location
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
        val sharedPref = requireActivity().getSharedPreferences("GamrPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("user_id", -1)

        if(currentUserId == -1) {
            Log.e("AUTH", "User ID not found. Redirect to login maybe?")
            return
        }

        val swipeHistory = remember { mutableStateListOf<Swipe>() }
        var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
        val coroutineScope = rememberCoroutineScope()

        val selectedPlaystles by filterViewModel.selectedPlaystyles.collectAsState()
        val selectedGenres by filterViewModel.selectedGenres.collectAsState()

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
        // Now also checks if the swipe was a match by checking the feedback from submit_swipe.php
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
                    val response = RetrofitClient.apiService.submitSwipe( // initialize submit_swipe.php
                        swiperId = currentUserId,
                        swipeeId = swipee.user_id,
                        direction = direction
                    )
                    if (response.isSuccessful) {
                        val body = response.body() // check the output of submit_swipe
                        Log.d("API", "Swipe response: $body") // body will be 'swiped left/right' unless it is a match


                        if (body?.get("message") == "Match created") { // body is "Match created" if both users swiped right

                            Toast.makeText(requireContext(), "It's a Match!", Toast.LENGTH_SHORT).show() //toast shows text at bottom of screen
                        }                                                                                // Brayden will replace it with a card for redirect to matches shortly
                    } else {
                        Log.e("API", "Swipe failed: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("API", "Swipe error: ${e.message}")
                }
            }
        }

        // Filter out already swiped users and apply the playstyle and genre filters
        val recommendedUsers = allUsers.filter { user ->
            swipeHistory.none { it.swipeeId == user.user_id && it.swiperId == currentUserId }
                    && (selectedPlaystles.isEmpty() || selectedPlaystles.contains(user.preferred_playstyle))
                    && (selectedGenres.isEmpty() || selectedGenres.contains(user.current_game_genre))
        }

        // Sort users by distance from the current user
        // If location cannot be accessed, distance is set to 0.0 and algorithm displays users
        // in order of their appearance on the database
        val sortedUsers = recommendedUsers.map { user ->
            val distance = currentLatitude?.let { lat1 ->
                currentLongitude?.let { lon1 ->
                    calculateDistance(lat1, lon1, user.latitude ?: 0.0, user.longitude ?: 0.0)
                }
            } ?: 0.0
            user to distance // Pairs user with its calculated distance
        }.sortedBy { it.second } // Sorts by distance in ascending order.

        // Display users sorted by distance
        if (sortedUsers.isNotEmpty()) {
            val user = sortedUsers.first().first // First user will have shortest distance
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
                        user = user.toLocalUser(requireContext()),
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
            Text("No more users to display", style = MaterialTheme.typography.bodyLarge, color= MaterialTheme.colorScheme.onBackground)
        }
    }

    // Profile card with swiping functionality
    // Detects gestures, if a certain amount of movement is detected, perform swipes.
    // Information displayed is stored in ProfileCard composable
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

    // Determine a profile's city.
    // Makes use of android's geocoder
    // Stores a city and state as strings if an address can be found.
    // Returns 'Unknown' if location cannot be accessed.
    // AI assistance provided me with a 'deprecated' function, I am commiting with these
    // for now and revisiting these functions after work is done on my second user story.
    fun getNearestTown(context: Context, latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault()) // initialize the geocoder
            val addresses = geocoder.getFromLocation(latitude, longitude, 1) // receives an address matching coordinates
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

    // Calculates distance between two coordinates using a known algorithm entitled "Haversine formula"
    // ChatGPT assisted in getting it written in Kotlin
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusMiles = 3958.8 // Earth radius in miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2).pow(2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusMiles * c
    }

    // Profile card defines defines the information that will be displayed on the card.
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
                Text(text = user.distanceInMiles?.let { "Distance: ${it.roundToInt()} mi" }?: "Distance: Unknown")
            }
        }
    }

    // User.toLocalUser accesses profile coordinates and current user coordinates to calculate distance
    // as the card appears in the feed
    // Returns an instance of LocalUser with all associated information.
    fun User.toLocalUser(context: Context): LocalUser {
        val latitude = this.latitude ?: 0.0
        val longitude = this.longitude ?: 0.0
        val currentLat = currentLatitude ?: 0.0
        val currentLon = currentLongitude ?: 0.0

        val distance = if (currentLatitude != null && currentLongitude != null &&
            this.latitude != null && this.longitude != null
        ) {
            calculateDistance(currentLat, currentLon, latitude, longitude)
        } else null

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
            nearestTown = getNearestTown(context, latitude, longitude),
            distanceInMiles = distance
        )
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001 // Location permissions require a request code.
    }
    }
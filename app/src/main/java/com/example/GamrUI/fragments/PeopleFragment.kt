package com.example.GamrUI.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import coil.compose.AsyncImage
import com.example.GamrUI.*
import com.example.GamrUI.R
import com.example.GamrUI.ui.theme.GamrUITheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

// ChatGPT assisted in various aspects of this fragment
// Particularly in regard to card display/formatting, distance and map formulas, & animations

// This is the local UI model for the class LocalUser

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
    val distanceInMiles: Double? = null,
    val profileImageUrl: String? = null
)

// Store a user's choice on another user
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
        // Initialize the FusedLocationProvider client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        // Grab the current user location
        fetchUserLocation()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                GamrUITheme {
                    ProfileFeedScreen()
                }
            }
        }
    }

    // Location Permissions are initialized in AndroidManifest and requested here
    // Makes use of FusedLocationProviderClient to grab current location
    // AI assisted with the use of "Context", it's purpose is to assist in accessing resources
    // (like location) from outside of the fragment.
    private fun fetchUserLocation() {
        if (
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions uses a deprecated java function, but works on most all android versions
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Error detection - Virtual Devices don't like using location, so work with null locations
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLatitude = it.latitude
                currentLongitude = it.longitude
            }
        }.addOnFailureListener {
            Log.e("LOCATION", "Error: ${it.message}")
        }
    }

    @Composable
    fun ProfileFeedScreen() {
        val context = LocalContext.current
        val sharedPref = requireActivity().getSharedPreferences("GamrPrefs", android.content.Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("user_id", -1)

        if (currentUserId == -1) {
            Log.e("AUTH", "User ID not found.")
            return
        }

        val swipeHistory = remember { mutableStateListOf<Swipe>() }
        var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
        val coroutineScope = rememberCoroutineScope()

        // tracking variables for if the card should be shown
        var showMatchCard by remember { mutableStateOf(false) }
        var matchedUser by remember { mutableStateOf<User?>(null) }

        val selectedPlaystyles by filterViewModel.selectedPlaystyles.collectAsState()
        val selectedGenres by filterViewModel.selectedGenres.collectAsState()

        // Fetch user feed once the screen is loaded
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                val response = RetrofitClient.apiService.getUserFeed(currentUserId)
                if (response.isSuccessful) {
                    allUsers = response.body() ?: emptyList()
                }
            }
        }

        // Handles user's choice on another user and sends it to the server
        fun handleSwipe(swipee: User, direction: String) {
            swipeHistory.add(Swipe(currentUserId, swipee.user_id, direction))
            coroutineScope.launch {
                try {
                    val response = RetrofitClient.apiService.submitSwipe(currentUserId, swipee.user_id, direction)
                    val body = response.body()
                    if (response.isSuccessful && body?.get("message") == "Match created") {
                        matchedUser = swipee
                        showMatchCard = true
                    }
                } catch (_: Exception) {}
            }
        }

        // Filter out already swiped users and apply the playstyle and genre filters
        val recommendedUsers = allUsers.filter { user ->
            swipeHistory.none { it.swipeeId == user.user_id && it.swiperId == currentUserId } &&
                    (selectedPlaystyles.isEmpty() || selectedPlaystyles.contains(user.preferred_playstyle)) &&
                    (selectedGenres.isEmpty() || selectedGenres.contains(user.current_game_genre))
        }


        // Sort users by distance from current user
        // If location cannot be accessed, distance is set to 0.0 and algorithm displays
        // in order of their appearance on the database.
        val sortedUsers = recommendedUsers.map { user ->
            val distance = currentLatitude?.let { lat1 ->
                currentLongitude?.let { lon1 ->
                    calculateDistance(lat1, lon1, user.latitude ?: 0.0, user.longitude ?: 0.0)
                }
            } ?: 0.0
            user to distance
        }.sortedBy { it.second }

        Box(modifier = Modifier.fillMaxSize()) {
            // Display users sorted by distance
            if (sortedUsers.isNotEmpty()) {
                val user = sortedUsers.first().first
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
                            user = user.toLocalUser(context),
                            onSwipe = { direction -> handleSwipe(user, direction) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { handleSwipe(user, "dislike") }) { Text("Dislike") }
                        Button(onClick = { handleSwipe(user, "like") }) { Text("Like") }
                    }
                }
            } else {
                Text(
                    text = "No more users to display",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Match card shown when a mutual like is detected
            if (showMatchCard && matchedUser != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎮 It’s a Match!", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("You matched with ${matchedUser?.gamertag}!", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(onClick = { showMatchCard = false }) {
                                    Text("Keep Swiping")
                                }
                                Button(onClick = {
                                    showMatchCard = false
                                    parentFragmentManager.commit {
                                        replace(R.id.fragment_container, ChatWindowFragment.newInstance(matchedUser!!))
                                        addToBackStack(null)
                                    }
                                }) {
                                    Text("Chat Now")
                                }
                            }
                        }
                    }
                }
            }
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
                    detectHorizontalDragGestures { _, dragAmount -> offsetX += dragAmount }
                },
            contentAlignment = Alignment.Center
        ) {
            ProfileCard(user = user, modifier = Modifier.offset { IntOffset(animatableOffsetX.value.toInt(), 0) })
        }
    }

    // Profile card defines the information that will be displayed on the card.
    @Composable
    fun ProfileCard(user: LocalUser, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .height(500.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column {
                if (user.profileImageUrl != null) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(250.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_profile),
                        contentDescription = "Default Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(250.dp)
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = user.gamertag, style = MaterialTheme.typography.titleLarge)
                    Text("Name: ${user.name}")
                    Text("Age: ${user.age}")
                    Text("Style: ${user.preferredPlaystyle}")
                    Text("Game: ${user.currentGame}")
                    Text("Bio: ${user.bio}")
                    Text("City: ${user.nearestTown ?: "Unknown"}")
                    Text("Distance: ${user.distanceInMiles?.roundToInt() ?: "Unknown"} mi")
                }
            }
        }
    }

    // User.toLocalUser accesses profile coordinates and current user coordinate to calculate distance
    // as the card appears in the feed
    // Returns an instance of LocalUser with all associated information.

    fun User.toLocalUser(context: android.content.Context): LocalUser {
        val distance = if (latitude != null && longitude != null && currentLatitude != null && currentLongitude != null)
            calculateDistance(currentLatitude!!, currentLongitude!!, latitude, longitude)
        else null

        return LocalUser(
            userId = user_id,
            gamertag = gamertag ?: "Unknown",
            name = name ?: "Unknown",
            age = age ?: 0,
            preferredPlaystyle = preferred_playstyle ?: "Unknown",
            currentGame = current_game ?: "Unknown",
            bio = bio ?: "",
            latitude = latitude,
            longitude = longitude,
            nearestTown = getNearestTown(context, latitude ?: 0.0, longitude ?: 0.0),
            distanceInMiles = distance,
            profileImageUrl = image_url
        )
    }

    // Calculates distance between two coordinates using an algorithm called "Haversine formula"
    // ChatGPT assisted in getting it written in Kotlin
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 3958.8
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2).pow(2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    // Determine a profile's city.
    // Makes use of android's geocoder
    // Stores a city and state as strings if an address can be found.
    // Returns 'Unknown' if location cannot be accessed.
    // AI assistance provided me with a 'deprecated' function, I am commiting with these as they work for most versions
    fun getNearestTown(context: android.content.Context, latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val city = addresses[0].locality ?: addresses[0].subAdminArea
                val state = addresses[0].adminArea
                if (city != null && state != null) "$city, $state" else city ?: state ?: "Unknown"
            } else "Unknown"
        } catch (e: IOException) {
            e.printStackTrace()
            "Unknown"
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001 // Location permissions require a request code
    }
}

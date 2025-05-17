package com.example.GamrUI.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.GamrUI.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MatchListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    // Inflates the fragment and creates the recycleview layout (see xml for more)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_match_list, container, false)
        recyclerView = view.findViewById(R.id.matchRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // loads matches from the database
        loadMatches()

        return view
    }

    // Retrieve a user's matches from the database.
    private fun loadMatches() {
        val sharedPref = requireActivity().getSharedPreferences("GamrPrefs", android.content.Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("user_id", -1)

        // Assistance from ChatGPT reccomended this failsafe for if no user is logged in.
        // Error checking doesn't hurt, but I've yet to access this page without logging in.
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Call get_matches.php through retrofit.
        RetrofitClient.apiService.getMatches(currentUserId)
            .enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful) {
                        val matches = response.body()?.toMutableList() ?: mutableListOf()

                        // Single dummy match for visual / testing purposes
                        val dummyUser = User(
                            user_id = 999,
                            gamertag = "GamrDev",
                            name = "Dev",
                            age = 99,
                            preferred_playstyle = "Casual",
                            current_game = "Developing Gamr",
                            current_game_genre = "Testing",
                            bio = "I'm here for test cases",
                            latitude = null,
                            longitude = null
                        )
                        matches.add(dummyUser)

                        // Recyclerviews are good for long scrollable lists.
                        // When a user taps their match, open the ChatWindowFragment
                        recyclerView.adapter = MatchAdapter(matches) { user ->
                            parentFragmentManager.commit {
                                replace(R.id.fragment_container, ChatWindowFragment.newInstance(user))
                                addToBackStack(null) // allows user to use android's "back" button
                            }
                        }
                    } else {
                        // error handling
                        Toast.makeText(requireContext(), "Failed to load matches", Toast.LENGTH_SHORT).show()
                    }
                }

                // have toast show api error
                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // binds matched users to their cards
    class MatchAdapter(
        private val matches: List<User>,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

        // defines the included fields for each card
        class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val username: TextView = view.findViewById(R.id.matchUsername)
            val game: TextView = view.findViewById(R.id.matchGame)
            val style: TextView = view.findViewById(R.id.matchStyle)
        }

        // inflate the item_match.xml for each card
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_match, parent, false)
            return MatchViewHolder(view)
        }

        // insert the data and listen for a click to send to the chat window
        override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
            val user = matches[position]
            holder.username.text = user.gamertag
            holder.game.text = "Game: ${user.current_game}"
            holder.style.text = "Style: ${user.preferred_playstyle}"
            holder.itemView.setOnClickListener { onItemClick(user) }
        }

        override fun getItemCount() = matches.size
    }
}

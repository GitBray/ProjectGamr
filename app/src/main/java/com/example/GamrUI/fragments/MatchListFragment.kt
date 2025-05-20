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
        val emptyText = view.findViewById<TextView>(R.id.emptyText) // if there are no matches, display this text
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // call loadmatches to grab a user's matches
        loadMatches { matches ->
            if (matches.isEmpty()) { // if the matchlist is empty, hide the view
                                    // and show the emptyText 'no matches'
                recyclerView.visibility = View.GONE
                emptyText.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyText.visibility = View.GONE
                recyclerView.adapter = MatchAdapter(matches) { user -> // pass matches to matchAdapter
                    parentFragmentManager.commit { // when users taps a match, open ChatWindowFragment instance for the user.
                        replace(R.id.fragment_container, ChatWindowFragment.newInstance(user))
                        addToBackStack(null)
                    }
                }
            }
        }
        return view
    }

    // Retrieve a user's matches from the database.
    private fun loadMatches(onResult: (List<User>) -> Unit) {
        val sharedPref = requireActivity().getSharedPreferences("GamrPrefs", android.content.Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("user_id", -1)

        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
            return
        }

        // call get_matches.php
        RetrofitClient.apiService.getMatches(currentUserId)
            .enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful) {
                        val matches = response.body() ?: emptyList()
                        onResult(matches)
                    } else {
                        Toast.makeText(requireContext(), "Failed to load matches", Toast.LENGTH_SHORT).show()
                        onResult(emptyList())
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    onResult(emptyList())
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

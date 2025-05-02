package com.example.GamrUI.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.GamrUI.R
import com.example.GamrUI.User

class MatchListFragment : Fragment() {

    // Dummy matches - same format as the database
    // Pulls from the database will be implemented at a later time.
    private val dummyMatches = listOf(
        User(
            user_id = 11,
            gamertag = "GrizzlyGamer",
            name = "Ros",
            age = 21,
            preferred_playstyle = "Competitive",
            current_game = "Street Fighter 6",
            current_game_genre = "Fighting",
            bio = "Fight me.",
            latitude = 32.0593,
            longitude = -93.6991
        ),
        User(
            user_id = 12,
            gamertag = "Dactyl",
            name = "Abby",
            age = 19,
            preferred_playstyle = "Competitive",
            current_game = "Overwatch 2",
            current_game_genre = "FPS",
            bio = "Pocket me",
            latitude = 32.7820,
            longitude = -92.1481
        )
    )

    // Layout created in XML, inflate the XML it when Fragment is called
    // Makes use of a RecyclerView, more efficient for long lists of matches.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_match_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.matchRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MatchAdapter(dummyMatches) { user ->
            // When a user taps their match, a new instance of ChatWindowFragment is created.
            parentFragmentManager.commit {
                replace(R.id.fragment_container, ChatWindowFragment.newInstance(user))
                addToBackStack(null) // allows for android's back button to work on chats
            }
        }
        return view
    }

    // MatchAdapter binds user data to the xml file item_match.xml
    // item_match.xml is the object for 'match' cards
    class MatchAdapter(
        private val matches: List<User>,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

        class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val username: TextView = view.findViewById(R.id.matchUsername)
            val game: TextView = view.findViewById(R.id.matchGame)
            val style: TextView = view.findViewById(R.id.matchStyle)

        }

        // inflates the item_match.xml item into the RecyclerView
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_match, parent, false)
            return MatchViewHolder(view)
        }

        // actually binds the user data and listens for a click
        // click passes which user was clicked as it's parameter
        override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
            val user = matches[position]
            holder.username.text = user.gamertag
            holder.game.text = "Game: ${user.current_game}"
            holder.style.text = "Style: ${user.preferred_playstyle}"
            holder.itemView.setOnClickListener { onItemClick(user) }
        }
        override fun getItemCount() = matches.size // item count tells RecyclerView how many items
                                                  // it's working with
    }
}

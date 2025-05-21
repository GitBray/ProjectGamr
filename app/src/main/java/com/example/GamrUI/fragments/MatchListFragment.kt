package com.example.GamrUI.fragments

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.GamrUI.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MatchListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_match_list, container, false)
        recyclerView = view.findViewById(R.id.matchRecyclerView)
        val emptyText = view.findViewById<TextView>(R.id.emptyText)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadMatches { matches ->
            if (matches.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyText.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyText.visibility = View.GONE
                recyclerView.adapter = MatchAdapter(matches) { user ->
                    parentFragmentManager.commit {
                        replace(R.id.fragment_container, ChatWindowFragment.newInstance(user))
                        addToBackStack(null)
                    }
                }
            }
        }
        return view
    }

    private fun loadMatches(onResult: (List<User>) -> Unit) {
        val sharedPref = requireActivity().getSharedPreferences("GamrPrefs", android.content.Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("user_id", -1)

        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
            return
        }

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

    class MatchAdapter(
        private val matches: List<User>,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

        class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val profileImage: ImageView = view.findViewById(R.id.matchImage)
            val username: TextView = view.findViewById(R.id.matchUsername)
            val game: TextView = view.findViewById(R.id.matchGame)
            val style: TextView = view.findViewById(R.id.matchStyle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_match, parent, false)
            return MatchViewHolder(view)
        }

        override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
            val user = matches[position]
            holder.username.text = user.gamertag
            holder.game.text = "Game: ${user.current_game}"
            holder.style.text = "Style: ${user.preferred_playstyle}"

            // Load profile image with Glide
            Glide.with(holder.itemView.context)
                .load(user.image_url)
                .placeholder(R.drawable.default_profile)
                .into(holder.profileImage)

            holder.itemView.setOnClickListener { onItemClick(user) }
        }

        override fun getItemCount() = matches.size
    }
}

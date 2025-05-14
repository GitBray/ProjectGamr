package com.example.GamrUI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.GamrUI.R
import androidx.fragment.app.activityViewModels
import com.example.GamrUI.FilterViewModel
import android.widget.ToggleButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch




class ExploreFragment : Fragment() {
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Each value is made for calling from FilterViewModel to connect the buttons
        // from the explore fragment and have it affect the people fragment
        val casualButton = view.findViewById<ToggleButton>(R.id.casualButton)
        val competitiveButton = view.findViewById<ToggleButton>(R.id.competitiveButton)
        val rankedButton = view.findViewById<ToggleButton>(R.id.rankedButton)
        val coopButton = view.findViewById<ToggleButton>(R.id.coopButton)

        val fightingButton = view.findViewById<ToggleButton>(R.id.fightingButton)
        val fpsButton = view.findViewById<ToggleButton>(R.id.fpsButton)
        val mobaButton = view.findViewById<ToggleButton>(R.id.mobaButton)
        val racingButton = view.findViewById<ToggleButton>(R.id.racingButton)

        casualButton.setOnClickListener {
            filterViewModel.togglePlaystyle("Casual")

        }

        competitiveButton.setOnClickListener {
            filterViewModel.togglePlaystyle("Competitive")

        }

        rankedButton.setOnClickListener {
            filterViewModel.togglePlaystyle("Ranked")

        }

        coopButton.setOnClickListener {
            filterViewModel.togglePlaystyle("Co-op")

        }

        fightingButton.setOnClickListener {
            filterViewModel.toggleGenre("Fighting")

        }

        fpsButton.setOnClickListener {
            filterViewModel.toggleGenre("FPS")

        }

        mobaButton.setOnClickListener {
            filterViewModel.toggleGenre("MOBA")

        }

        racingButton.setOnClickListener {
            filterViewModel.toggleGenre("Racing")

        }

        val playstyles = filterViewModel.selectedPlaystyles.value
        casualButton.isChecked = "Casual" in playstyles
        competitiveButton.isChecked = "Competitive" in playstyles
        rankedButton.isChecked = "Ranked" in playstyles
        coopButton.isChecked = "Co-op" in playstyles

        val genres = filterViewModel.selectedGenres.value
        fightingButton.isChecked = "Fighting" in genres
        fpsButton.isChecked = "FPS" in genres
        mobaButton.isChecked = "MOBA" in genres
        racingButton.isChecked = "Racing" in genres

        viewLifecycleOwner.lifecycleScope.launch {
            filterViewModel.selectedPlaystyles.collect { playstyles ->
                casualButton.isChecked = "Casual" in playstyles
                competitiveButton.isChecked = "Competitive" in playstyles
                rankedButton.isChecked = "Ranked" in playstyles
                coopButton.isChecked = "Co-op" in playstyles
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            filterViewModel.selectedGenres.collect { genres ->
                fightingButton.isChecked = "Fighting" in genres
                fpsButton.isChecked = "FPS" in genres
                mobaButton.isChecked = "MOBA" in genres
                racingButton.isChecked = "Racing" in genres
            }
        }

    }
}
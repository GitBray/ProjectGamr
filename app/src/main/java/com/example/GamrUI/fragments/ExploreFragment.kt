package com.example.GamrUI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.GamrUI.R
import androidx.fragment.app.activityViewModels
import com.example.GamrUI.FilterViewModel
import android.widget.Button


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
        val casualButton = view.findViewById<Button>(R.id.casualButton)
        val competitiveButton = view.findViewById<Button>(R.id.competitiveButton)
        val rankedButton = view.findViewById<Button>(R.id.rankedButton)
        val coopButton = view.findViewById<Button>(R.id.coopButton)

        val fightingButton = view.findViewById<Button>(R.id.fightingButton)
        val fpsButton = view.findViewById<Button>(R.id.fpsButton)
        val mobaButton = view.findViewById<Button>(R.id.mobaButton)
        val racingButton = view.findViewById<Button>(R.id.racingButton)

        casualButton.setOnClickListener {
            filterViewModel.togglePlaystyle("Casual")
            casualButton.isSelected = !casualButton.isSelected
        }

        competitiveButton.setOnClickListener {
            filterViewModel.togglePlaystyle("Competitive")
            competitiveButton.isSelected = !competitiveButton.isSelected
        }

        rankedButton.setOnClickListener {
            filterViewModel.togglePlaystyle("Ranked")
            rankedButton.isSelected = !rankedButton.isSelected
        }

        coopButton.setOnClickListener {
            filterViewModel.togglePlaystyle("Co-op")
            coopButton.isSelected = !coopButton.isSelected
        }

        fightingButton.setOnClickListener {
            filterViewModel.toggleGenre("Fighting")
            fightingButton.isSelected = !fightingButton.isSelected
        }

        fpsButton.setOnClickListener {
            filterViewModel.toggleGenre("FPS")
            fpsButton.isSelected = !fpsButton.isSelected
        }

        mobaButton.setOnClickListener {
            filterViewModel.toggleGenre("MOBA")
            mobaButton.isSelected = !mobaButton.isSelected
        }

        racingButton.setOnClickListener {
            filterViewModel.toggleGenre("Racing")
            racingButton.isSelected = !racingButton.isSelected
        }
    }

}
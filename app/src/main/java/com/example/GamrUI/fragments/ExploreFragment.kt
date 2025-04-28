package com.example.GamrUI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.GamrUI.R
import androidx.fragment.app.activityViewModels
import com.example.GamrUI.FilterViewModel

class ExploreFragment : Fragment() {
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val casualButton = view.findViewById<View>(R.id.casualButton)
        casualButton.setOnClickListener{
            filterViewModel.togglePlaystyle("Casual")
        }

        val competitiveButton = view.findViewById<View>(R.id.competitiveButton)
        competitiveButton.setOnClickListener{
            filterViewModel.togglePlaystyle("Competitive")
        }

        val rankedButton = view.findViewById<View>(R.id.rankedButton)
        rankedButton.setOnClickListener{
            filterViewModel.togglePlaystyle("Ranked")
        }

        val coopButton = view.findViewById<View>(R.id.coopButton)
        coopButton.setOnClickListener{
            filterViewModel.togglePlaystyle("CO-OP")
        }
    }
}
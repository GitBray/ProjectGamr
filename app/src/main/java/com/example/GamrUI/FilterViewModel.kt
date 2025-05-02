package com.example.GamrUI

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//This folder is for connecting the PeopleFragment and ExploreFragment

class FilterViewModel : ViewModel(){
    //backing state for playstyle filters
    private val _selectedPlaystyles = MutableStateFlow<Set<String>>(emptySet())
    val selectedPlaystyles: StateFlow<Set<String>> get() = _selectedPlaystyles

    //backing state for genres filters
    private val _selectedGenres = MutableStateFlow<Set<String>>(emptySet())
    val selectedGenres: StateFlow<Set<String>> get() = _selectedGenres

    //toggles playstyle and if already selected, remove it
    fun togglePlaystyle(style: String){
        val updated = _selectedPlaystyles.value.toMutableSet()
        if (updated.contains(style)){
            updated.remove(style)
        } else{
            updated.add(style)
        }
        _selectedPlaystyles.value = updated
    }
    //same logic for genre
    fun toggleGenre(genre: String){
        val updated = _selectedGenres.value.toMutableSet()
        if (updated.contains(genre)) updated.remove(genre)
        else {
            updated.add(genre)
        }
        _selectedGenres.value = updated
    }

    fun clearAll(){
        _selectedPlaystyles.value = emptySet()
        _selectedGenres.value = emptySet()
    }
}
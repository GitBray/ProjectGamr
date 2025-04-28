package com.example.GamrUI

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//This folder is for connecting the PeopleFragment and ExploreFragment

class FilterViewModel : ViewModel(){
    private val _selectedPlaystyles = MutableStateFlow<Set<String>>(emptySet())
    val selectedPlaystyles: StateFlow<Set<String>> get() = _selectedPlaystyles

    fun togglePlaystyle(style: String){
        val updated = _selectedPlaystyles.value.toMutableSet()
        if (updated.contains(style)){
            updated.remove(style)
        } else{
            updated.add(style)
        }
        _selectedPlaystyles.value = updated
    }

    fun clearAll(){
        _selectedPlaystyles.value = emptySet()
    }
}
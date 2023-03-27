package com.example.smarcifier.ui.temp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TempViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "36.5"
    }
    val text: LiveData<String> = _text
}
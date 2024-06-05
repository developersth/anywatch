package com.anydev.anywatch.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.starmax.bluetoothsdk.data.CallControlType

class CallControlViewModel(

) : ViewModel() {
    var callControlType by mutableStateOf(CallControlType.Answer)

    var callNumber by mutableStateOf("")
    var exitNumber by mutableStateOf("")
}
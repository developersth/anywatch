package com.anydev.anywatch.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.starmax.bluetoothsdk.StarmaxMapResponse
import com.starmax.bluetoothsdk.data.NotifyType
import org.json.JSONObject

class HealthOpenViewModel(

) : ViewModel() {
    var heartRate by mutableStateOf(false)

    var bloodPressure by mutableStateOf(false)

    var bloodOxygen by mutableStateOf(false)

    var pressure by mutableStateOf(false)

    var temp by mutableStateOf(false)

    var bloodSugar by mutableStateOf(false)

    fun setHealthOpen(response: StarmaxMapResponse){
        if(response.type == NotifyType.GetHealthOpen){
            val result = response.obj!!
            heartRate = result.get("heart_rate") as Boolean
            bloodPressure = result.get("blood_pressure") as Boolean
            bloodOxygen = result.get("blood_oxygen") as Boolean
            pressure = result.get("pressure") as Boolean
            temp = result.get("temp") as Boolean
            bloodSugar = result.get("blood_sugar") as Boolean
        }
    }
}
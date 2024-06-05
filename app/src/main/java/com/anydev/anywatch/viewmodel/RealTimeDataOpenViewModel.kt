package com.anydev.anywatch.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.starmax.bluetoothsdk.StarmaxMapResponse
import com.starmax.bluetoothsdk.data.NotifyType
import org.json.JSONObject

class RealTimeDataOpenViewModel(

) : ViewModel() {
    var gsensor by mutableStateOf(false)

    var steps by mutableStateOf(false)

    var heartRate by mutableStateOf(false)

    var bloodPressure by mutableStateOf(false)

    var bloodOxygen by mutableStateOf(false)

    var temp by mutableStateOf(false)

    var bloodSugar by mutableStateOf(false)

    fun setRealTimeDataOpen(response: StarmaxMapResponse){
        if(response.type == NotifyType.GetRealTimeOpen){
            val result = response.obj!!
            gsensor = result.get("gsensor") as Boolean
            steps = result.get("steps") as Boolean
            heartRate = result.get("heart_rate") as Boolean
            bloodPressure = result.get("blood_pressure") as Boolean
            bloodOxygen = result.get("blood_oxygen") as Boolean
            temp = result.get("temp") as Boolean
            bloodSugar = result.get("blood_sugar") as Boolean
        }
    }
}
package com.anydev.anywatch.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.starmax.bluetoothsdk.StarmaxMapResponse
import com.starmax.bluetoothsdk.data.NotifyType

class HeartRateViewModel : ViewModel(){
    var startHour by mutableStateOf(0)

    var startMinute by mutableStateOf(0)

    var endHour by mutableStateOf(0)

    var endMinute by mutableStateOf(0)

    var period by mutableStateOf(10)

    var alarmThreshold by mutableStateOf(1000)

    fun setStartTime(picker: MaterialTimePicker){
        startHour = picker.hour
        startMinute = picker.minute
    }

    fun setEndTime(picker: MaterialTimePicker){
        endHour = picker.hour
        endMinute = picker.minute
    }

    fun setHeartRate(response: StarmaxMapResponse){
        if(response.type == NotifyType.GetHeartRate){
            val result = response.obj!!
            startHour = result.get("start_hour") as Int
            startMinute = result.get("start_minute") as Int
            endHour = result.get("end_hour") as Int
            endMinute = result.get("end_minute") as Int
            period = result.get("period") as Int
            alarmThreshold = result.get("alarm_threshold") as Int
        }
    }
}
package com.anydev.anywatch.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.starmax.bluetoothsdk.StarmaxMapResponse
import com.starmax.bluetoothsdk.data.NotifyType

class UserInfoViewModel(

) : ViewModel() {
    var sex by mutableStateOf(0)

    var age by mutableStateOf(0)

    var height by mutableStateOf(0)

    var weight by mutableStateOf(0)

    fun setUserInfo(response: StarmaxMapResponse){
        if(response.type == NotifyType.GetUserInfo){
            val result = response.obj!!
            sex = result["sex"] as Int
            age = result["age"] as Int
            height = result["height"] as Int
            weight = result["weight"] as Int
        }
    }
}
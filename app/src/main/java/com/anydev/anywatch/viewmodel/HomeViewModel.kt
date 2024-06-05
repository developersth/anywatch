package com.anydev.anywatch.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.anydev.anywatch.NavPage

class HomeViewModel(

) : ViewModel() {
    var openNetDialog by mutableStateOf(false)
        private set

    var openStateDialog by mutableStateOf(false)
        private set

    var openHeartRateDialog by mutableStateOf(false)
        private set

    var openRealTimeDataDialog by mutableStateOf(false)
        private set

    var openContactDialog by mutableStateOf(false)
        private set

    var openSosDialog by mutableStateOf(false)
        private set

    var openNotDisturbDialog by mutableStateOf(false)
        private set

    fun toggleSetNet(){
        openNetDialog = !openNetDialog
    }

    fun toggleSetState(){
        openStateDialog = !openStateDialog
    }


    fun toggleHeartRateOpen(){
        openHeartRateDialog = !openHeartRateDialog
    }

    fun toggleRealTimeDataOpen(){
        openRealTimeDataDialog = !openRealTimeDataDialog
    }


    fun toScan(navController: NavController) {
        navController.navigate(NavPage.ScanPage.name)
    }
}
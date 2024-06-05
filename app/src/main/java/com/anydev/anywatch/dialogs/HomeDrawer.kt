package com.anydev.anywatch.dialogs

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.anydev.anywatch.viewmodel.BleViewModel
import com.anydev.anywatch.viewmodel.HomeViewModel
import com.anydev.anywatch.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawer(drawerState: DrawerState) {
    val context = LocalContext.current

    val bleViewModel: BleViewModel = if(context is ComponentActivity) viewModel(context) else viewModel()
    val viewModel: HomeViewModel = if(context is ComponentActivity) viewModel(context) else viewModel()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    HomeDrawerView(context = context, drawerState = drawerState, viewModel = viewModel, bleViewModel = bleViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawerView(context: Context,drawerState: DrawerState,viewModel: HomeViewModel,bleViewModel: BleViewModel) {
    val scope = rememberCoroutineScope()
    val activity = if(context is AppCompatActivity) context else null
    val selectBinLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.binUri = uri
            bleViewModel.sendCustomDial(context)
        }


    val selectImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.imageUri = uri
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectBinLauncher.launch(intent)
        }

    val selectDialLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.binUri = uri
            bleViewModel.sendDialLocal(context)
        }

    DismissibleNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet(
                modifier = Modifier
                    .width(200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                IconButton(onClick = {
                    scope.launch {
                        drawerState.close()
                    }
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "ปิด")
                }
                Divider()
                NavigationDrawerItem(label = { Text(text = "ตั้งค่าเซิร์ฟเวอร์") }, onClick = {
                    viewModel.toggleSetNet()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "การจับคู่อุปกรณ์") }, onClick = {
                    bleViewModel.pair()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "ตั้งค่าอุปกรณ์") }, onClick = {
                    viewModel.toggleSetState()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "ค้นหาอุปกรณ์") }, onClick = {
                    bleViewModel.findDevice()
                    scope.launch {
                        drawerState.close()
                    }
                },  selected = false)
                NavigationDrawerItem(label = { Text(text = "รับข้อมูลปริมาณพลังงาน") }, onClick = {
                    bleViewModel.getPower()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "รับข้อมูลรุ่น") }, onClick = {
                    bleViewModel.getVersion()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "ซิงค์เวลา") }, onClick = {
                    bleViewModel.setTime()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "รับข้อมูลสุขภาพปัจจุบัน") }, onClick = {
                    bleViewModel.getHealthDetail()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "เปิด/ปิดสวิตช์ข้อมูลแบบเรียลไทม์") }, onClick = {
                    viewModel.toggleRealTimeDataOpen()
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
                NavigationDrawerItem(label = { Text(text = "ส่งข้อความ") }, onClick = {
                    bleViewModel.sendMessage()
                    scope.launch {
                        drawerState.close()
                    }
                },selected = false)
                NavigationDrawerItem(label = { Text(text = "ปิดเครื่อง") }, onClick = {
                    if (bleViewModel.bleDevice?.get() != null) {
                        bleViewModel.close()
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }, selected = false)
            }
        }) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewHomeDrawer() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val context = LocalContext.current
    HomeDrawerView(context = context, drawerState = drawerState, viewModel = HomeViewModel(), bleViewModel = BleViewModel())
}
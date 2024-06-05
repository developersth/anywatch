package com.anydev.anywatch.pages

import android.Manifest
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material.icons.twotone.Bluetooth
import androidx.compose.material.icons.twotone.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anydev.anywatch.ui.theme.AppTheme
import com.anydev.anywatch.viewmodel.BleViewModel
import com.anydev.anywatch.viewmodel.ScanViewModel
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

// Request code for enabling Bluetooth
private const val REQUEST_ENABLE_BT = 1
@Composable
fun ScanPage(navController: NavController, viewModel: ScanViewModel = viewModel()) {
    val context = LocalContext.current
    val bleViewModel: BleViewModel = viewModel(LocalContext.current as ComponentActivity)

    // Remember Bluetooth status
    val isBluetoothEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Check if Bluetooth is enabled
        isBluetoothEnabled.value = isBluetoothEnabled(context)

        // Request to enable Bluetooth if it's not enabled
        if (!isBluetoothEnabled.value) {
            enableBluetooth(context)
        }

        // Start scan only if Bluetooth is enabled
        if (isBluetoothEnabled.value) {
            viewModel.startScan()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopScan()
        }
    }

    ScanPageView(navController, viewModel, bleViewModel)
}

// Function to check if Bluetooth is enabled
private fun isBluetoothEnabled(context: Context): Boolean {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    return bluetoothAdapter?.isEnabled == true
}

// Function to request enabling Bluetooth
private fun enableBluetooth(context: Context) {
    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    if (context is Activity) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPageView(navController: NavController, viewModel: ScanViewModel = viewModel(), bleViewModel: BleViewModel) {
    var isLoading by remember { mutableStateOf(false) }

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "เลือกอุปกรณ์")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "Back")
                        }
                    },
                )
            }
        ) { innerPadding ->

            LazyColumn(
                contentPadding = innerPadding
            ) {
                items(viewModel.devices.size) { index ->
                    ListItem(
                        modifier = Modifier.clickable {
                            isLoading = true
                            bleViewModel.connect(viewModel.devices[index])
                            navController.popBackStack()

                        },
                        headlineContent = { Text(text = viewModel.getDeviceName(index)) },
                        overlineContent = { Text(text = viewModel.devices[index].mac) },
                        supportingContent = {
                            val broadcastText = viewModel.broadcast[viewModel.devices[index].mac]
                            broadcastText?.let { Text(text = it) }
                        },
                        leadingContent = { Icon(Icons.TwoTone.Bluetooth, contentDescription = null) },
                        trailingContent = {
                            Icon(
                                Icons.TwoTone.SignalCellularAlt,
                                contentDescription = null
                            )
                        }
                    )
                    Divider()
                }
            }
        }

        LoadingDialog(isLoading = isLoading, context = LocalContext.current, "Connecting...")
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewScanPage() {
    val navController = rememberNavController()
    ScanPageView(navController,ScanViewModel(),BleViewModel())
}

@Composable
fun LoadingDialog(isLoading: Boolean, context: Context,title:String) {
    if (isLoading) {
        val progressDialog = remember { ProgressDialog(context) }
        DisposableEffect(Unit) {
            progressDialog.setMessage(title) // Set your message here
            progressDialog.setCancelable(false) // Set cancelable or not
            progressDialog.show()

            onDispose {
                progressDialog.dismiss()
            }
        }
    }
}



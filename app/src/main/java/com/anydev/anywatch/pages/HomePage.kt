package com.anydev.anywatch

import android.text.Layout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anydev.anywatch.dialogs.*
import com.anydev.anywatch.viewmodel.BleViewModel
import com.anydev.anywatch.viewmodel.HomeViewModel
import com.anydev.anywatch.viewmodel.SetNetModel
import com.anydev.anywatch.dialogs.SetNetDialog
import com.anydev.anywatch.ui.theme.AppTheme
import com.starmax.net.NetApi
import com.starmax.net.NetChannel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class)
@Composable
fun HomePage(navController: NavController) {

    val context = LocalContext.current
    val netViewModel: SetNetModel =
        if (context is ComponentActivity) viewModel(context) else viewModel()
    val bleViewModel: BleViewModel =
        if (context is ComponentActivity) viewModel(context) else viewModel()
    val viewModel: HomeViewModel =
        if (context is ComponentActivity) viewModel(context) else viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = SnackbarHostState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)


    if (context is ComponentActivity) {
        LaunchedEffect(Unit) {
            bleViewModel.initPath(context)
        }
    }
        AppTheme {
            if (viewModel.openNetDialog) {
                SetNetDialog()
            }
            if (viewModel.openStateDialog) {
                SetStateDialog()
            }
            if (viewModel.openHeartRateDialog) {
                SetHeartDialog()
            }
            if (viewModel.openRealTimeDataDialog) {
                SetRealTimeDataOpenDialog()
            }
            // Function to handle connection button click
            val onConnectionButtonClick: () -> Unit = if (bleViewModel.bleConnectStatus) {
                // Disconnect if connected
                { bleViewModel.disconnect() }
            } else {
                // Connect if not connected
                { viewModel.toScan(navController) }
            }
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },
                topBar = {
                    SmallTopAppBar(
                        title = {
                            Text(text = "Any WatchApp")
                        },
                        actions = {
                            ConnectionButton(
                                isConnected = bleViewModel.bleConnectStatus,
                                onClick = onConnectionButtonClick
                            )

                        },
                        navigationIcon = {

                                IconButton(onClick = {
                                    scope.launch { drawerState.open()
                                    }
                                }) {
                                    Icon(Icons.TwoTone.Menu, contentDescription = "รายการคำสั่ง")
                                }
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            actionIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                },
                content = { innerPadding ->
                    LazyColumn(
                        contentPadding = innerPadding,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val bleDevice = bleViewModel.bleDevice
                            if (bleViewModel.bleConnectStatus){//check connection succuss
                                bleViewModel.pair();
                            }
                            if (bleDevice?.get() == null) {
                                ListItem(
                                    headlineContent = { Text(text = "Disconnect") },
                                    leadingContent = {
                                        Icon(
                                            Icons.TwoTone.Bluetooth,
                                            contentDescription = null
                                        )
                                    },
                                    trailingContent = {
                                        Icon(
                                            Icons.TwoTone.SignalCellularOff,
                                            contentDescription = null
                                        )
                                    }
                                )
                            } else {
                                ListItem(
                                    headlineContent = { Text(text = bleViewModel.getDeviceName()) },
                                    overlineContent = { Text(text = bleDevice.get()!!.mac) },
                                    leadingContent = {
                                        Icon(
                                            Icons.TwoTone.Bluetooth,
                                            contentDescription = null
                                        )
                                    },
                                    trailingContent = { Text(text = bleViewModel.bleStateLabel) }

                                )
                            }
                        }
                        item {
                            Divider()
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .padding(15.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = "Server: " + (if (netViewModel.server == NetApi.Server) "Public" else "Test") +
                                            " Channel: " + (if (netViewModel.channel == NetChannel.Release) "Release" else "Beta"),
                                    fontSize = TextUnit(12F, TextUnitType.Sp)
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .padding(15.dp)
                                    .fillMaxWidth()
                            ) {
                                bleViewModel.bleMessage.value.let {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = it,
                                        fontSize = TextUnit(12F, TextUnitType.Sp)
                                    )
                                }
                            }
                        }
                        item {
                            Divider()
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .padding(15.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(15.dp)
                                        .weight(1f),
                                    text = bleViewModel.bleResponseLabel + "\nข้อมูลดั้งเดิม：\n" + bleViewModel.originData.value
                                )
                            }
                        }
                    }
                }
            )
            HomeDrawer(drawerState)
        }



}



@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val navController = rememberNavController()
    HomePage(navController)
}

@Composable
fun ConnectionButton(isConnected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = { onClick() },
    ) {
            Text(
                text = if (isConnected) "ยกเลิกการเชื่อมต่อ" else "เพิ่มอุปกรณ์",
            )
            Icon(
                imageVector = if (isConnected) Icons.Default.Clear else Icons.Filled.Add,
                contentDescription = null,
            )
        }

}

package com.anydev.anywatch.dialogs

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anydev.anywatch.viewmodel.BleViewModel
import com.anydev.anywatch.viewmodel.HealthOpenViewModel
import com.anydev.anywatch.viewmodel.HomeViewModel
import com.anydev.anywatch.viewmodel.RealTimeDataOpenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetRealTimeDataOpenDialog() {
    val bleViewModel : BleViewModel = viewModel(LocalContext.current as ComponentActivity)
    val viewModel: RealTimeDataOpenViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel(LocalContext.current as ComponentActivity)

    bleViewModel.bleResponsePost.observeForever { message ->
        viewModel.setRealTimeDataOpen(message)
    }

    LaunchedEffect(Unit) {
        bleViewModel.getRealTimeOpen()
    }

    Dialog(
        onDismissRequest = { homeViewModel.toggleRealTimeDataOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "สวิทช์การตรวจวัดข้อมูลสุขภาพ",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "สวิทช์จำนวนก้าวทั้งหมด", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.steps, onCheckedChange = {
                        viewModel.steps = it
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "สวิทช์อัตราการเต้นของหัวใจทั้งหมด", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.heartRate, onCheckedChange = {
                        viewModel.heartRate = it
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "สวิทช์ความดันเลือดทั้งหมด", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.bloodPressure, onCheckedChange = {
                        viewModel.bloodPressure = it
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "สวิทช์ความออกซิเจนในเลือดทั้งหมด", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.bloodOxygen, onCheckedChange = {
                        viewModel.bloodOxygen = it
                    })
                }



                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "สวิทช์อุณหภูมิทั้งหมด", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.temp, onCheckedChange = {
                        viewModel.temp = it
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "สวิทช์น้ำตาลในเลือดทั้งหมด", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.bloodSugar, onCheckedChange = {
                        viewModel.bloodSugar = it
                    })
                }

                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleRealTimeDataOpen()
                    }) {
                        Text(text = "ยกเลิก")
                    }
                    ElevatedButton(
                        onClick = {
                            bleViewModel.setRealTimeOpen(
                                gsensor = viewModel.gsensor,
                                steps = viewModel.steps,
                                heartRate = viewModel.heartRate,
                                bloodPressure = viewModel.bloodPressure,
                                bloodOxygen = viewModel.bloodOxygen,
                                temp = viewModel.temp,
                                bloodSugar = viewModel.bloodSugar
                            )
                            homeViewModel.toggleRealTimeDataOpen()
                        }, colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.offset(15.dp)
                    ) {
                        Text(text = "ตกลง")
                    }
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewSetRealTimeDataOpenDialog() {
    SetRealTimeDataOpenDialog()
}
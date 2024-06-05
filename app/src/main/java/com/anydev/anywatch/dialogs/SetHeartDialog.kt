package com.anydev.anywatch.dialogs

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.anydev.anywatch.viewmodel.BleViewModel
import com.anydev.anywatch.viewmodel.HeartRateViewModel
import com.anydev.anywatch.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetHeartDialog() {
    val activity = LocalContext.current as AppCompatActivity
    val bleViewModel: BleViewModel = viewModel(activity)
    val viewModel: HeartRateViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel(activity)

    bleViewModel.bleResponsePost.observeForever { message ->
        viewModel.setHeartRate(message)
    }

    LaunchedEffect(Unit) {
        bleViewModel.getHeartRateControl()
    }

    Dialog(
        onDismissRequest = { homeViewModel.toggleHeartRateOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "ตั้งค่าระยะเวลาและช่วงการตรวจวัดอัตราการเต้นของหัวใจ",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "เวลาเริ่มต้น", style = MaterialTheme.typography.labelMedium)
                    TextButton(onClick = {
                        activity.let {
                            val picker = MaterialTimePicker.Builder().build()
                            picker.addOnPositiveButtonClickListener {
                                viewModel.setStartTime(picker)
                            }
                            picker.show(it.supportFragmentManager, picker.toString())
                        }
                    }) {
                        Text(text = "${viewModel.startHour}:${viewModel.startMinute}")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "เวลาสิ้นสุด", style = MaterialTheme.typography.labelMedium)
                    TextButton(onClick = {
                        activity.let {
                            val picker = MaterialTimePicker.Builder().build()
                            picker.addOnPositiveButtonClickListener {
                                viewModel.setEndTime(picker)
                            }
                            picker.show(it.supportFragmentManager, picker.toString())
                        }
                    }) {
                        Text(text = "${viewModel.endHour}:${viewModel.endMinute}")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ระยะเวลาการตรวจวัด", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.period.toString(), onValueChange = {
                        viewModel.period = it.toIntOrNull() ?: 180
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "ระยะเวลาการตรวจวัด(เป็นนาที)", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ค่าย่านการเตือนความดันโลหิตสถิต", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.alarmThreshold.toString(), onValueChange = {
                        viewModel.alarmThreshold = it.toIntOrNull() ?: 75
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "ค่าย่านการเตือนความดันโลหิตสถิต", style = MaterialTheme.typography.labelSmall)
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
                        homeViewModel.toggleHeartRateOpen()
                    }) {
                        Text(text = "ยกเลิก")
                    }
                    ElevatedButton(
                        onClick = {
                            bleViewModel.setHeartRateControl(
                                startHour = viewModel.startHour,
                                startMinute = viewModel.startMinute,
                                endHour = viewModel.endHour,
                                endMinute = viewModel.endMinute,
                                period = viewModel.period,
                                alarmThreshold = viewModel.alarmThreshold
                            )
                            homeViewModel.toggleHeartRateOpen()
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
fun PreviewSetHeartDialog() {
    SetHeartDialog()
}
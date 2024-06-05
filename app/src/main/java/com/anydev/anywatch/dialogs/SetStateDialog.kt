package com.anydev.anywatch.dialogs

import SetStateViewModel
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
import com.anydev.anywatch.viewmodel.HomeViewModel

@Composable
fun SetStateDialog() {
    val bleViewModel: BleViewModel = viewModel(LocalContext.current as ComponentActivity)
    val viewModel: SetStateViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel(LocalContext.current as ComponentActivity)

    bleViewModel.bleResponsePost.observeForever { message ->
        viewModel.setState(message)
    }

    LaunchedEffect(Unit) {
        bleViewModel.getState()
    }

    SetStateDialogView(homeViewModel = homeViewModel, bleViewModel = bleViewModel, viewModel = viewModel)
}
@Composable
fun SetStateDialogView(homeViewModel: HomeViewModel,bleViewModel: BleViewModel,viewModel: SetStateViewModel){
    Dialog(
        onDismissRequest = { homeViewModel.toggleSetState() }
    ) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "ตั้งค่าสถานะ",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ตั้งเวลา", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.timeFormat == 1, onCheckedChange = { it ->
                        viewModel.timeFormat = 1
                    }) {
                        Icon(
                            if (viewModel.timeFormat == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "12 ชั่วโมง"
                        )
                    }
                    Text(text = "12 ชั่วโมง", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.timeFormat == 0, onCheckedChange = { it ->
                        viewModel.timeFormat = 0
                    }) {
                        Icon(
                            if (viewModel.timeFormat == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "24 ชั่วโมง"
                        )
                    }
                    Text(text = "รูปแบบ 24 ชั่วโมง", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ตั้งหน่วย", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.unitFormat == 0, onCheckedChange = { it ->
                        viewModel.unitFormat = 0
                    }) {
                        Icon(
                            if (viewModel.unitFormat == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "ระบบเมตริก"
                        )
                    }
                    Text(text = "ระบบเมตริก", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.unitFormat == 1, onCheckedChange = { it ->
                        viewModel.unitFormat = 1
                    }) {
                        Icon(
                            if (viewModel.unitFormat == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "ระบบสิ่งอำนวยความสะดวก"
                        )
                    }
                    Text(text = "ระบบความสะดวก", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ตั้งค่าอุณหภูมิ", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.tempFormat == 0, onCheckedChange = { it ->
                        viewModel.tempFormat = 0
                    }) {
                        Icon(
                            if (viewModel.tempFormat == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "เซลเซียส"
                        )
                    }
                    Text(text = "เซลเซียส", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.tempFormat == 1, onCheckedChange = { it ->
                        viewModel.tempFormat = 1
                    }) {
                        Icon(
                            if (viewModel.tempFormat == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "ฟาเรนไฮต์"
                        )
                    }
                    Text(text = "ฟาเรนไฮต์", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ภาษา", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.language == 2, onCheckedChange = { it ->
                        viewModel.language = 2
                    }) {
                        Icon(
                            if (viewModel.language == 2) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "อังกฤษ"
                        )
                    }
                    Text(text = "อังกฤษ", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.language == 1, onCheckedChange = { it ->
                        viewModel.language = 1
                    }) {
                        Icon(
                            if (viewModel.language == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "ภาษาจีน"
                        )
                    }
                    Text(text = "ภาษาจีน", style = MaterialTheme.typography.labelSmall)
                    IconToggleButton(checked = viewModel.language == 11, onCheckedChange = { it ->
                        viewModel.language = 11
                    }) {
                        Icon(
                            if (viewModel.language == 11)Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "ไทย"
                        )
                    }
                    Text(text = "ไทย", style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ระยะเวลาในการสว่าง", style = MaterialTheme.typography.labelMedium)
                    Slider(value = viewModel.backlighting.toFloat(), onValueChange = {
                        viewModel.backlighting = it.toInt()
                    }, steps = 5, valueRange = 5f..25f, modifier = Modifier.offset(x = 15.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ความสว่างของหน้าจอ", style = MaterialTheme.typography.labelMedium)
                    Slider(value = viewModel.screen.toFloat(), onValueChange = {
                        viewModel.screen = it.toInt()
                    }, steps = 5, valueRange = 0f..60f, modifier = Modifier.offset(x = 15.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "สวิตช์การเปิดไฟด้วยการยกมือขึ้น", style = MaterialTheme.typography.labelMedium)
                    Switch(checked = viewModel.wristUp, onCheckedChange = {
                        viewModel.wristUp = it
                    })
                }
            }

                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleSetState()
                    }) {
                        Text(text = "ยกเลิก")
                    }
                    ElevatedButton(
                        onClick = {
                            bleViewModel.setState(
                                timeFormat = viewModel.timeFormat,
                                unitFormat = viewModel.unitFormat,
                                tempFormat = viewModel.tempFormat,
                                language = viewModel.language,
                                backlighting = viewModel.backlighting,
                                screen = viewModel.screen,
                                wristUp = viewModel.wristUp
                            )
                            homeViewModel.toggleSetState()
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


@Preview(showBackground = true)
@Composable
fun PreviewSetStateDialog() {
    SetStateDialogView(viewModel = SetStateViewModel(), bleViewModel = BleViewModel(), homeViewModel = HomeViewModel())
}
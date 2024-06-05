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
import com.starmax.net.NetApi
import com.starmax.net.NetChannel
import com.anydev.anywatch.viewmodel.HomeViewModel
import com.anydev.anywatch.viewmodel.SetNetModel

@Composable
fun SetNetDialog() {
    val context = LocalContext.current
    val viewModel: SetNetModel = viewModel(context as ComponentActivity)
    val homeViewModel: HomeViewModel = viewModel(context as ComponentActivity)

    LaunchedEffect(Unit) {
        viewModel.getData()
    }
    Dialog(
        onDismissRequest = { homeViewModel.toggleSetNet() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "Server Settings",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Server", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.server == NetApi.TestServer, onCheckedChange = { it ->
                        viewModel.setServerData(NetApi.TestServer)
                    }) {
                        Icon(
                            if (viewModel.server == NetApi.TestServer) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Test"
                        )
                    }
                    Text(text = "Test", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.server == NetApi.Server, onCheckedChange = { it ->
                        viewModel.setServerData(NetApi.Server)
                    }) {
                        Icon(
                            if (viewModel.server == NetApi.Server) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Release"
                        )
                    }
                    Text(text = "Release", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Channel", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.channel == NetChannel.Beta, onCheckedChange = { it ->
                        viewModel.setChannelData(NetChannel.Beta)
                    }) {
                        Icon(
                            if (viewModel.channel == NetChannel.Beta) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Beta"
                        )
                    }
                    Text(text = "Beta", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.channel == NetChannel.Release, onCheckedChange = { it ->
                        viewModel.setChannelData(NetChannel.Release)
                    }) {
                        Icon(
                            if (viewModel.channel == NetChannel.Release) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Public"
                        )
                    }
                    Text(text = "Public", style = MaterialTheme.typography.labelSmall)
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ElevatedButton(
                        onClick = {
                            homeViewModel.toggleSetNet()
                        }, colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.offset(15.dp)
                    ) {
                        Text(text = "OK")
                    }
                }
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun PreviewSetNetDialog() {
    SetNetDialog()
}
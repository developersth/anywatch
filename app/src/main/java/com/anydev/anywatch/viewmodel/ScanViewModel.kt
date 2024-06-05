package com.anydev.anywatch.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.starmax.bluetoothsdk.Utils

class ScanViewModel(

) : ViewModel() {
    var devices: List<BleDevice> by mutableStateOf(emptyList())
    var deviceNames : HashMap<String,String> = hashMapOf()
    var broadcast: HashMap<String,String> = hashMapOf()
    var searchName: String by mutableStateOf("")
    var isScanning: Boolean by mutableStateOf(false)

    fun getDeviceName(index:Int) : String {
        val name = devices[index].name;
        if(name != null){
            return name;
        }

        if(deviceNames.containsKey(devices[index].mac)){
            return deviceNames[devices[index].mac]!!
        }
        return "";
    }

    fun startScan() {
        val newDevices : MutableList<BleDevice> = mutableListOf()
        isScanning = true
        BleManager.getInstance().scan(object :BleScanCallback(){
            override fun onScanStarted(success: Boolean) {

            }

            override fun onScanning(bleDevice: BleDevice?) {
                if(bleDevice != null && bleDevice.rssi >= -90 && bleDevice.name != null && bleDevice.name?.contains(searchName) == true){
                    var isChecked = false
                    var i = 0
                    while (i < bleDevice.scanRecord.size - 1){
                        val len = bleDevice.scanRecord[i].toInt()
                        val type = bleDevice.scanRecord[i + 1].toUByte()
                        val rawData = bleDevice.scanRecord.slice(i + 2 until i + 1 + len).toByteArray()
                        i += 1 + len
                        if(type == 0xFF.toUByte()){
                            val firstData = rawData.slice(0 .. 1).toByteArray()
                            if(firstData.contentEquals(byteArrayOf(0x00,0x01))){
                                isChecked = true
                            }else if(firstData.contentEquals(byteArrayOf(0x00,0x02))){
                                if(rawData.slice(2 .. 3).toByteArray().contentEquals(byteArrayOf(
                                        0xAA.toByte(), 0xEE.toByte()
                                    ))) {

                                    broadcast[bleDevice.mac] =
                                        "SN:" + Utils.bytesToHex(
                                            rawData.slice(4..6).toByteArray().reversedArray()
                                        ) + "," +
                                                "Heart Rate:" + rawData[7].toInt()
                                            .toString() + "," +
                                                "Steps:" + Utils.byteArray2Sum(rawData.slice(8..10))
                                            .toString() + "," +
                                                "Blood Pressure:" + rawData[11].toInt()
                                            .toString() + "/" + rawData[12].toInt()
                                            .toString() + "," +
                                                "Blood Oxygen:" + rawData[13].toInt()
                                            .toString() + "," +
                                                "Blood Sugar:" + rawData[14].toInt()
                                            .toString() + "," +
                                                "Temperature:" + Utils.byteArray2Sum(
                                            rawData.slice(
                                                15..16
                                            )
                                        ).toString() + "," +
                                                "Metu:" + rawData[17].toInt().toString() + "," +
                                                "MAI:" + rawData[18].toInt().toString() + "," +
                                                "Pressure:" + rawData[19].toInt().toString() + "," +
                                                if (rawData.size > 20) {
                                                    "Calories:" + Utils.byteArray2Sum(
                                                        rawData.slice(
                                                            20..22
                                                        )
                                                    ).toString() + "," +
                                                            "Battery:" + rawData[23].toInt()
                                                        .toString()
                                                } else {
                                                    ""
                                                }
                                }

                                }
                        }
                    }

                    if(isChecked){
                        deviceNames[bleDevice.mac] = bleDevice.name
                        newDevices.add(bleDevice)
                    }

                    devices = newDevices.toList()
                }
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                isScanning = false
            }

        })
    }

    fun stopScan(){
        if(isScanning){
            BleManager.getInstance().cancelScan()
        }
    }
}
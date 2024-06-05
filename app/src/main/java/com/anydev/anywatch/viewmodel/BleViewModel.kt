package com.anydev.anywatch.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.starmax.bluetoothsdk.*
import com.starmax.bluetoothsdk.data.*
import com.starmax.net.repository.UiRepository
import com.anydev.anywatch.utils.NetFileUtils
import kotlinx.coroutines.awaitAll
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.lang.ref.SoftReference
import java.util.*
import kotlin.concurrent.schedule

enum class BleState {
    DISCONNECTED,
    CONNECTTING,
    CONNECTED
}

class BleViewModel() : ViewModel() {
    private var savePath = ""
    private var playTimer: Timer? = null

    val WriteServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9d")
    val WriteCharacteristicUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9d")


    val NotifyServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9d")
    val NotifyCharacteristicUUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9d")

    var bleDevice: SoftReference<BleDevice>? by mutableStateOf(null)
        private set

    var bleModel = ""
    var bleVersion = ""
    var bleUiVersion = ""
    var uiSupportDifferentialUpgrade = false

    val starmaxNotify = MapStarmaxNotify()

    var originData = mutableStateOf("")
        private set

    var bleState by mutableStateOf(BleState.DISCONNECTED)
        private set

    var isWear by mutableStateOf(1)
        private set

    var realTimeData by mutableStateOf("")
        private set

    var bleResponse by mutableStateOf(
        StarmaxMapResponse(
            obj = mapOf(),
            type = NotifyType.Failure
        )
    )
        private set

    var bleResponsePost = MutableLiveData(
        StarmaxMapResponse(
            obj = mapOf(),
            type = NotifyType.Failure
        )
    )

    var bleMessage = mutableStateOf("")
        private set

    val bleStateLabel: String
        get() {
            val data = when (bleState) {
                BleState.DISCONNECTED -> "ไม่ได้เชื่อมต่อ"
                BleState.CONNECTTING -> "กำลังเชื่อมต่อ"
                BleState.CONNECTED -> "เชื่อมต่อแล้ว"
            }
            return data
        }
    val bleConnectStatus: Boolean
        get() {
            var vCheck = when (bleState) {
                BleState.DISCONNECTED -> false
                BleState.CONNECTTING -> false
                BleState.CONNECTED -> true
            }
            return vCheck
        }
    val bleResponseLabel: String
        get() {
            val data = when (bleResponse.type) {
                NotifyType.Failure -> "การส่งล้มเหลว"
                NotifyType.Pair -> "การจับคู่สำเร็จ"
                NotifyType.GetState -> "การรับข้อมูลสถานะสำเร็จ"
                NotifyType.SetState -> "การตั้งค่าสถานะสำเร็จ"
                NotifyType.FindDevice -> "การค้นหาอุปกรณ์สำเร็จ"
                NotifyType.CameraControl -> "การควบคุมกล้องถ่ายรูปสำเร็จ"
                NotifyType.PhoneControl -> "การควบคุมโทรศัพท์สำเร็จ"
                NotifyType.Power -> notifyPower()
                NotifyType.Version -> notifyVersion()
                NotifyType.SetTime -> "การปรับเวลาสำเร็จ"
                NotifyType.GetUserInfo -> "การรับข้อมูลผู้ใช้สำเร็จ"
                NotifyType.SetUserInfo -> "การตั้งค่าข้อมูลผู้ใช้สำเร็จ"
                NotifyType.SetGoals -> "การตั้งเป้าหมายกีฬาสำเร็จ"
                NotifyType.HealthDetail -> notifyHealthDetail()
                NotifyType.GetHealthOpen -> "การรับข้อมูลเปิดใช้งานสุขภาพสำเร็จ"
                NotifyType.SetHealthOpen -> "การตั้งค่าเปิดใช้งานสุขภาพสำเร็จ"
                NotifyType.GetHeartRate -> "การรับข้อมูลระยะเวลาและขอบเขตการตรวจวัดอัตราการเต้นหัวใจสำเร็จ"
                NotifyType.SetHeartRate -> "การตั้งค่าระยะเวลาและขอบเขตการตรวจวัดอัตราการเต้นหัวใจสำเร็จ"
                NotifyType.GetContact -> "การรับข้อมูลผู้ติดต่อที่ใช้บ่อยสำเร็จ"
                NotifyType.SetContact -> "การตั้งค่าผู้ติดต่อที่ใช้บ่อยสำเร็จ"
                NotifyType.SetSos -> "การตั้งค่าผู้ติดต่อฉุกเฉินสำเร็จ"
                NotifyType.GetNotDisturb -> "การตั้งค่าโหมดห้ามรบกวนสำเร็จ"
                NotifyType.SetNotDisturb -> "การตั้งค่าโหมดห้ามรบกวนสำเร็จ"
                NotifyType.GetClock -> "การรับข้อมูลนาฬิกาปลุกสำเร็จ"
                NotifyType.SetClock -> "การตั้งค่านาฬิกาปลุกสำเร็จ"
                NotifyType.GetLongSit -> "การรับข้อมูลการนั่งนานๆสำเร็จ"
                NotifyType.SetLongSit -> "การตั้งค่าการนั่งนานๆสำเร็จ"
                NotifyType.GetDrinkWater -> "การรับข้อมูลการดื่มน้ำสำเร็จ"
                NotifyType.SetDrinkWater -> "การตั้งค่าการดื่มน้ำสำเร็จ"
                NotifyType.SendMessage -> "การส่งข้อความสำเร็จ"
                NotifyType.SetWeather -> "การส่งข้อมูลสภาพอากาศสำเร็จ"
                NotifyType.SetRealTimeOpen -> "การเปิดข้อมูลแบบเรียลไทม์สำเร็จ"
                NotifyType.RealTimeData -> notifyRealTimeData()
                NotifyType.WristDetachment -> notifyWristDetachment()
                NotifyType.GetEventReminder -> notifyEventReminder()
                NotifyType.SetEventReminder -> "การตั้งค่าการแจ้งเตือนเหตุการณ์สำเร็จ"
                NotifyType.GetSportMode -> notifySportMode()
                NotifyType.SetSportMode -> "การตั้งค่าโหมดกีฬาสำเร็จ"
                NotifyType.SportHistory -> notifySportHistory()
                NotifyType.StepHistory -> notifyStepHistory()
                NotifyType.HeartRateHistory -> notifyHeartRateHistory()
                NotifyType.BloodPressureHistory -> notifyBloodPressureHistory()
                NotifyType.BloodOxygenHistory -> notifyBloodOxygenHistory()
                NotifyType.PressureHistory -> notifyPressureHistory()
                NotifyType.MetHistory -> notifyMetHistory()
                NotifyType.TempHistory -> notifyTempHistory()
                NotifyType.Mai -> notifyMaiHistory()
                NotifyType.BloodSugarHistory -> notifyBloodSugarHistory()
                NotifyType.ValidHistoryDates -> notifyValidHistoryDates()
                NotifyType.DialInfo -> notifyDialInfo()
                NotifyType.SwitchDial -> "การสลับหน้าปัด5001สำเร็จ"
                NotifyType.Log -> "การรับข้อมูล Log"
                NotifyType.CloseDevice -> "การปิดอุปกรณ์สำเร็จ"
                else -> "สถานะที่ไม่รู้จัก"
            }
            return data
        }

    var imageUri: Uri? = null
    var binUri: Uri? = null

    var bleGattCallback: BleGattCallback = object : BleGattCallback() {
        override fun onStartConnect() {
            bleState = BleState.CONNECTTING
            bleMessage.value = "กำลังเชื่อมต่อกับอุปกรณ์ Bluetooth"
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            bleState = BleState.DISCONNECTED
            bleMessage.value = "การเชื่อมต่อกับอุปกรณ์ Bluetooth ล้มเหลว" + exception.toString()
        }

        override fun onConnectSuccess(
            bleDevice: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            bleState = BleState.CONNECTED
            bleMessage.value = "การเชื่อมต่อกับอุปกรณ์ Bluetooth สำเร็จ"

            openNotify(bleDevice)
        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            bleState = BleState.DISCONNECTED
            bleMessage.value = "การเชื่อมต่อกับอุปกรณ์ Bluetooth ถูกตัดการเชื่อมต่อ"
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {

        }

    }

    fun getDeviceName() : String {
        val name = bleDevice?.get()?.name;
        if(name != null){
            return name;
        }

        return "";
    }

    fun initPath(context: Context){
        var basepath = context.getExternalFilesDir(null)?.path
        if(basepath == null){
            basepath = Environment.getExternalStorageDirectory().absolutePath
        }
        savePath = basepath + "/SDKDemo/Device_update/"
        println("ที่อยู่ดาวน์โหลด："+savePath)
    }


    fun connect(newBleDevice: BleDevice?) {
        bleDevice = SoftReference(newBleDevice)
        Log.e("HomeViewModel", bleDevice.toString())
        if (bleDevice != null) {
             BleManager.getInstance().connect(bleDevice!!.get(), bleGattCallback)
        }
    }
    fun disconnect() {
       //bleDevice = SoftReference(newBleDevice)
        Log.e("HomeViewModel", bleDevice.toString())
        if (bleDevice != null) {
            BleManager.getInstance().disconnect(bleDevice!!.get())
        }
    }
    fun openNotify(newBleDevice: BleDevice?) {
        BleManager.getInstance().notify(
            newBleDevice,
            NotifyServiceUUID.toString(),
            NotifyCharacteristicUUID.toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    bleMessage.value = "เปิดการแจ้งเตือนสำเร็จ"
                    changeMtu {}
                }

                override fun onNotifyFailure(exception: BleException) {
                    bleMessage.value = "เปิดการแจ้งเตือนไม่สำเร็จ: $exception"
                }

                @SuppressLint("MissingPermission", "NewApi")
                override fun onCharacteristicChanged(data: ByteArray) {
                    bleResponse = starmaxNotify.notify(data) as StarmaxMapResponse
                    bleResponsePost.postValue(bleResponse)

                    if (bleResponse.type == NotifyType.RealTimeData) {
                        startTimer()
                    } else if (bleResponse.type == NotifyType.Log) {
                    } else if (bleResponse.type != NotifyType.SendFile && bleResponse.type != NotifyType.CrcFailure) {
                        originData.value = starmaxNotify.originData.map { String.format("%02X", it) }.toString()
                    }
                }
            })
    }

    private fun startTimer() {
        stopTimer()
        playTimer = Timer()
        playTimer?.schedule(2000){
            bleResponse = StarmaxMapResponse(
                obj = mapOf(
                    "gsensor_list" to null,
                    "steps" to null,
                    "calore" to null,
                    "distance" to null,
                    "heart_rate" to null,
                    "blood_pressure_ss" to null,
                    "blood_pressure_fz" to null,
                    "blood_oxygen" to null,
                    "temp" to null,
                    "blood_sugar" to null
                ),
                type = NotifyType.RealTimeData
            )
        }
    }

    private fun stopTimer(){
        playTimer?.cancel()
        playTimer = null
    }
    fun pair() {
        val data = StarmaxSend().pair()
        sendMsg(data)
    }

    fun getState() {
        val data = StarmaxSend().getState()
        sendMsg(data)
    }

    fun setState(
        timeFormat: Int,
        unitFormat: Int,
        tempFormat: Int,
        language: Int,
        backlighting: Int,
        screen: Int,
        wristUp: Boolean
    ) {
        val data = StarmaxSend().setState(
            timeFormat = timeFormat,
            unitFormat = unitFormat,
            tempFormat = tempFormat,
            language = language,
            backlighting = backlighting,
            screen = screen,
            wristUp = wristUp
        )
        sendMsg(data)
    }

    fun findDevice() {
        val data = StarmaxSend().findDevice(isFind = true)
        sendMsg(data)
    }

    fun cameraControl(cameraControlType: CameraControlType) {
        val data = StarmaxSend().cameraControl(cameraControlType)
        sendMsg(data)
    }

    fun callControl(callControlType: CallControlType, incomingNumber: String, exitNumber: String) {
        val number = when (callControlType) {
            CallControlType.Incoming -> incomingNumber
            CallControlType.Exit -> exitNumber
            else -> ""
        }

        val data = StarmaxSend().phoneControl(callControlType, number, true)
        sendMsg(data)
    }

    fun getPower() {
        val data = StarmaxSend().getPower()
        sendMsg(data)
    }

    fun notifyPower(): String {
        val data = bleResponse.obj!!
        return ("พลังงาน:" + data["power"].toString() + "\n"
                + "กำลังชาร์จ:" + data["is_charge"].toString())
    }


    fun getVersion() {
        val data = StarmaxSend().getVersion()
        sendMsg(data)
    }

    fun notifyVersion(): String {
        val data = bleResponse.obj!!

        bleModel = data["model"] as String
        bleVersion = data["version"] as String
        bleUiVersion = data["ui_version"] as String
        uiSupportDifferentialUpgrade = data["ui_support_differential_upgrade"] as Boolean

        return ("เวอร์ชัน Firmware: " + bleVersion + "\n"
                + "เวอร์ชัน UI: " + bleUiVersion + "\n"
                + "ขนาดบัฟเฟอร์ที่อุปกรณ์รองรับ:" + data["buffer_size"] + "\n"
                + "ความกว้างของหน้าจอ LCD:" + data["lcd_width"] + "\n"
                + "ความสูงของหน้าจอ LCD:" + data["lcd_height"] + "\n"
                + "ประเภทหน้าจอ:" + data["screen_type"] + "\n"
                + "โมเดลของอุปกรณ์:" + bleModel + "\n"
                + "UI การอัปเดตแบบบังคับ:" + data["ui_force_update"] + "\n"
                + "รองรับการอัปเดตแบบทดแทน:" + uiSupportDifferentialUpgrade + "\n"
                + "รองรับการตรวจวัดระดับน้ำตาลในเลือด:" + data["support_sugar"] + "\n"
                )
    }


    fun getRealTimeOpen() {
        val data = StarmaxSend().getRealTimeOpen()
        sendMsg(data)
    }

    fun setRealTimeOpen(
        gsensor: Boolean,
        steps: Boolean,
        heartRate: Boolean,
        bloodPressure: Boolean,
        bloodOxygen: Boolean,
        temp: Boolean,
        bloodSugar: Boolean
    ) {
        val data = StarmaxSend().setRealTimeOpen(
            gsensor,
            steps,
            heartRate,
            bloodPressure,
            bloodOxygen,
            temp,
            bloodSugar
        )
        sendMsg(data)
    }

    fun setTime() {
        val data = StarmaxSend().setTime()
        sendMsg(data)
    }

    fun getUserInfo() {
        val data = StarmaxSend().getUserInfo()
        sendMsg(data)
    }

    fun setUserInfo(
        sex: Int,
        age: Int,
        height: Int,
        weight: Int,
    ) {
        val data = StarmaxSend().setUserInfo(
            sex = sex,
            age = age,
            height = height,
            weight = weight
        )
        sendMsg(data)
    }

    fun getGoals() {
        val data = StarmaxSend().getGoals()
        sendMsg(data)
    }

    fun setGoals(
        steps: Int,
        heat: Int,
        distance: Int
    ) {
        val data = StarmaxSend().setGoals(
            steps = steps,
            heat = heat,
            distance = distance
        )
        sendMsg(data)
    }

    fun getHealthDetail() {
        val data = StarmaxSend().getHealthDetail()
        sendMsg(data)
    }

    fun notifyHealthDetail(): String {
        val data = bleResponse.obj!!
        return ("จำนวนก้าวทั้งหมด:" + data["total_steps"] + "\n"
                + "แคลอรี่ทั้งหมด(กิโลแคลอรี่):" + data["total_heat"] + "\n"
                + "ระยะทางทั้งหมด(เมตร):" + data["total_distance"] + "\n"
                + "รวมเวลานอนหลับ(นาที):" + data["total_sleep"] + "\n"
                + "เวลานอนหลับลึก:" + data["total_deep_sleep"] + "\n"
                + "เวลานอนหลับเบา:" + data["total_light_sleep"] + "\n"
                + "อัตราการเต้นของหัวใจปัจจุบัน:" + data["current_heart_rate"] + "\n"
                + "ความดันเลือดปัจจุบัน:" + data["current_ss"] + " /" + data["current_fz"] + "\n"
                + "ออกซิเจนในเลือดปัจจุบัน:" + data["current_blood_oxygen"] + "\n"
                + "ระดับความเครียดปัจจุบัน:" + data["current_pressure"] + "\n"
                + "ดัชนีมวลกล้ามเนื้อปัจจุบัน:" + data["current_mai"] + "\n"
                + "การเผาผลาญพลังงานในหนึ่งชั่วโมงปัจจุบัน:" + data["current_met"] + "\n"
                + "อุณหภูมิปัจจุบัน:" + data["current_temp"] + "\n"
                + "ระดับน้ำตาลในเลือดปัจจุบัน:" + data["current_blood_sugar"] + "\n"
                )
    }


    fun getHealthOpen() {
        val data = StarmaxSend().getHealthOpen()
        sendMsg(data)
    }

    fun setHealthOpen(
        heartRate: Boolean,
        bloodPressure: Boolean,
        bloodOxygen: Boolean,
        pressure: Boolean,
        temp: Boolean,
        bloodSugar: Boolean
    ) {
        val data = StarmaxSend().setHealthOpen(
            heartRate = heartRate,
            bloodPressure = bloodPressure,
            bloodOxygen = bloodOxygen,
            pressure = pressure,
            temp = temp,
            bloodSugar = bloodSugar
        )
        sendMsg(data)
    }

    fun getHeartRateControl() {
        val data = StarmaxSend().getHeartRateControl()
        sendMsg(data)
    }

    fun setHeartRateControl(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        period: Int,
        alarmThreshold: Int
    ) {
        val data = StarmaxSend().setHeartRateControl(
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            period = period,
            alarmThreshold = alarmThreshold
        )
        sendMsg(data)
    }

    fun getContacts() {
        val data = StarmaxSend().getContacts()
        sendMsg(data)
    }

    fun setContacts(contacts: List<Contact>) {
        val data = StarmaxSend().setContacts(contacts)
        sendMsg(data)
    }

    fun setSos(contacts: List<Contact>) {
        val data = StarmaxSend().setSos(contacts)
        sendMsg(data)
    }

    fun getClock() {
        val data = StarmaxSend().getClock()
        sendMsg(data)
    }

    fun setClock() {
        val data = StarmaxSend().setClock(
            clocks = arrayListOf(
                Clock(9, 0, true, intArrayOf(1, 1, 0, 1, 0, 1, 0), 0),
                Clock(11, 45, true, intArrayOf(1, 1, 0, 1, 0, 1, 0), 0),
                Clock(18, 0, false, intArrayOf(1, 1, 0, 1, 0, 1, 0), 0)
            )
        )
        sendMsg(data)
    }

    fun getLongSit() {
        val data = StarmaxSend().getLongSit()
        sendMsg(data)
    }

    fun setLongSit() {
        val data = StarmaxSend().setLongSit(
            true,
            9,
            0,
            18,
            0,
            15
        )
        sendMsg(data)
    }

    fun getDrinkWater() {
        val data = StarmaxSend().getDrinkWater()
        sendMsg(data)
    }

    fun setDrinkWater() {
        val data = StarmaxSend().setDrinkWater(
            true,
            9,
            0,
            18,
            0,
            15
        )
        sendMsg(data)
    }

    fun getNotDisturb() {
        val data = StarmaxSend().getNotDisturb()
        sendMsg(data)
    }

    fun setNotDisturb(
        onOff: Boolean,
        allDayOnOff: Boolean,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        val data = StarmaxSend().setNotDisturb(
            onOff = onOff,
            allDayOnOff = allDayOnOff,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
        )
        sendMsg(data)
    }

    fun sendMessage() {
        val data = StarmaxSend()
            .sendMessage(MessageType.Mail, "มีเพื่อนส่งรูปภาพมา" + "1213", "[12132312]" + "รูปภาพ jpg คุณสามารถดูตัวอย่างของรูปภาพได้ที่นี่: (ใส่ URL ของภาพ)")
        sendMsg(data)
    }

    fun setWeather() {
        val data = StarmaxSend().setWeather(
            weatherDays = arrayListOf(
                WeatherDay(29, 30, 28, 10, 60, 30, 9, 2, 3),
                WeatherDay(26, 30, 27, 10, 30, 30, 9, 2, 1),
                WeatherDay(31, 31, 28, 10, 10, 30, 9, 2, 4),
                WeatherDay(31, 31, 28, 10, 10, 30, 9, 2, 6)
            )
        )
        sendMsg(data)
    }
    fun sendCustomDial(context: Context) {
        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                val img = context.contentResolver.openInputStream(imageUri!!) as FileInputStream?
                BleFileSender.initFileWithBackground(
                    bin,
                    240, 282,
                    img,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {}

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "Current Progress ${progress.toInt()}%"
                        }

                        override fun onFailure() {}

                        override fun onStart() {
                            val data = StarmaxSend()
                                .sendDial(
                                    5001,
                                    BmpUtils.bmp24to16(255, 255, 255),
                                    1
                                )
                            sendMsg(data)
                        }

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()
                                sendMsg(data)
                            }
                        }
                    })

                BleFileSender.sliceBuffer = 10

                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "Server Error"
                e.printStackTrace()
            }
        }
    }

    fun sendMusic() {
        val data = StarmaxSend().musicControl(1, 20, 30, "kasd asssssa", "adsadsd")
        sendMsg(data)
    }

    fun sendNumber() {
        val data = StarmaxSend().phoneControl(CallControlType.Incoming, "13783679375", true)
    }

    fun notifyRealTimeData(): String {
        val data = bleResponse.obj
        realTimeData = JSONObject(data).toString()

        return Date().toString() +":\n"+"สถานะการสวมใส่:${isWear}\n" +realTimeData
    }

    fun notifyWristDetachment(): String {
        val data = JSONObject(bleResponse.obj)
        val is_wear = data.get("is_wear") as Int
        isWear = is_wear

        return Date().toString() +":\n"+"สถานะการสวมใส่:${isWear}\n" +realTimeData
    }


    fun getReminder() {
        val data = StarmaxSend().getEventReminder()
        sendMsg(data)
    }

    fun setReminder() {
        val calendar = Calendar.getInstance()
        val data = StarmaxSend().setEventReminder(
            listOf(
                EventReminder(
                    calendar,
                    "Travel with friends",
                    1,
                    1,
                    intArrayOf(),
                ),
                EventReminder(
                    calendar,
                    "Travel with friends",
                    2,
                    2,
                    intArrayOf(),
                ),
                EventReminder(
                    calendar,
                    "ไปเที่ยวกับเพื่อน",
                    3,
                    3,
                    intArrayOf(0, 1, 0, 1, 0, 1, 0)
                ),
                EventReminder(
                    calendar,
                    "ไปเที่ยวกับเพื่อน",
                    4,
                    4,
                    intArrayOf(),
                )
            )
        )
        sendMsg(data)
    }

    fun notifyEventReminder(): String {
        val data = bleResponse.obj!!

        println(data.toString())
        var str = ""

        val reminderList = data.get("event_reminders") as ArrayList<*>
        for (i in 0 until reminderList.size) {
            val oneData = reminderList[i] as HashMap<*, *>
            str += ("Time:" + oneData["year"] + "-" + oneData["month"] + "-" + oneData["day"] + oneData["hour"] + ":" + oneData["minute"]
                    + "Content:" + oneData["content"] + "\n"
                    )
        }

        return str
    }


    fun getSportMode(){
        val data = StarmaxSend().getSportMode()
        sendMsg(data)
    }

    fun setSportMode() {
        val data = StarmaxSend().setSportMode(modes = listOf(
            0x0A,
            0x0B,
            0x0C,
            0x0D
        ))
        sendMsg(data)
    }

    fun notifySportMode(): String {
        val data = bleResponse.obj!!
        val status = data.get("status") as Int

        if (status == 0) {
            var str = ""

            val dataList = data["sport_modes"] as ArrayList<Int>
            for (i in 0 until dataList.size) {
                str += "Sport Mode: ${sportModeLabel(dataList[i])}\n"
            }

            return str
        } else {
            return statusLabel(status)
        }
    }


    fun getSportHistory() {
        val data = StarmaxSend().getSportHistory()
        sendMsg(data)
    }

    fun notifySportHistory(): String {
        val data = bleResponse.obj
        val str = data.toString()

        return str
    }

    fun getStepHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        val data = StarmaxSend().getStepHistory(calendar)
        sendMsg(data)
    }

    fun notifyStepHistory(): String {
        val data = bleResponse.obj!!

        println(data.toString())
        var str = ("Sampling interval: " + data["interval"] + " minutes\n"
                + "Date: " + data["year"] + "-" + data["month"] + "-" + data["day"] + "\n"
                + "Data length: " + data["data_length"] + "\n"
                )

        val stepList = data.get("step_list") as ArrayList<*>
        for (i in 0 until stepList.size) {
            val oneData = stepList[i] as HashMap<*, *>
            str += ("Time: " + oneData["hour"] + ":" + oneData["minute"]
                    + " Steps: " + oneData["steps"]
                    + ", Calories: " + ((oneData["calorie"] as Int).toDouble() / 1000) + " kcal"
                    + ", Distance: " + ((oneData["distance"] as Int).toDouble() / 100) + " meters\n")
        }

        val sleepList = data["sleep_list"] as ArrayList<*>
        for (i in 0 until sleepList.size) {
            val oneData = sleepList[i] as HashMap<*, *>
            str += ("Time: " + oneData["hour"] + ":" + oneData["minute"]
                    + " Sleep status: " + oneData["sleep_status"] + "\n")
        }

        return str
    }


    fun getHeartRateHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        changeMtu {
            val data = StarmaxSend().getHeartRateHistory(calendar)
            sendMsg(data)
        }
    }

    fun notifyHeartRateHistory(): String {
        val data = bleResponse.obj!!
        var str = ("Sampling interval: " + data["interval"] + " minutes\n"
                + "Date: " + data["year"] + "-" + data["month"] + "-" + data["day"] + "\n"
                + "Data length: " + data["data_length"] + "\n"
                )

        val dataList = data["heart_rate_list"] as ArrayList<*>
        for (i in 0 until dataList.size) {
            val oneData = dataList[i] as HashMap<*, *>
            str += "Time: " + oneData["hour"] + ":" + oneData["minute"] + " Heart rate: " + oneData["heart_rate"] + "\n"
        }

        return str
    }


    fun getBloodPressureHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        changeMtu {
            val data = StarmaxSend().getBloodPressureHistory(calendar)
            sendMsg(data)
        }
    }

    fun notifyBloodPressureHistory(): String {
        val data = bleResponse.obj!!
        var str = ("Sampling interval: " + data["interval"] + " minutes\n"
                + "Date: " + data["year"] + "-" + data["month"] + "-" + data["day"] + "\n"
                + "Data length: " + data["data_length"] + "\n"
                )

        val dataList = data["blood_pressure_list"] as ArrayList<*>
        for (i in 0 until dataList.size) {
            val oneData = dataList[i] as HashMap<*, *>
            str += "Time: " + oneData["hour"] + ":" + oneData["minute"] + " Systolic pressure: " + oneData["ss"] + " Diastolic pressure: " + oneData["fz"] + "\n"
        }

        return str
    }


    fun getBloodOxygenHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        changeMtu {
            val data = StarmaxSend().getBloodOxygenHistory(calendar)
            sendMsg(data)
        }
    }

    fun notifyBloodOxygenHistory(): String {
        val data = bleResponse.obj!!
        val status = data["status"] as Int
        if (status == 0) {
            var str = ("Sampling interval: " + data["interval"] + " minutes\n"
                    + "Date: " + data["year"] + "-" + data["month"] + "-" + data["day"] + "\n"
                    + "Data length: " + data["data_length"] + "\n"
                    )

            val dataList = data["blood_oxygen_list"] as ArrayList<*>
            for (i in 0 until dataList.size) {
                val oneData = dataList[i] as HashMap<*, *>
                str += "Time: " + oneData["hour"] + ":" + oneData["minute"] + " Blood oxygen: " + oneData["blood_oxygen"] + "%\n"
            }

            return str
        } else {
            return statusLabel(status)
        }
    }


    fun getPressureHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        changeMtu {
            val data = StarmaxSend().getPressureHistory(calendar)
            sendMsg(data)
        }
    }

    fun notifyPressureHistory(): String {
        val data = bleResponse.obj!!
        val status = data["status"] as Int
        if (status == 0) {
            var str = ("Sampling interval: " + data["interval"] + " minutes\n"
                    + "Date: " + data["year"] + "-" + data["month"] + "-" + data["day"] + "\n"
                    + "Data length: " + data["data_length"] + "\n"
                    )

            val dataList = data["pressure_list"] as ArrayList<*>
            for (i in 0 until dataList.size) {
                val oneData = dataList[i] as HashMap<*, *>
                str += "Time: " + oneData["hour"] + ":" + oneData["minute"] + " Pressure: " + oneData["pressure"] + "\n"
            }

            return str
        } else {
            return statusLabel(status)
        }
    }


    fun getMetHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        changeMtu {
            val data = StarmaxSend().getMetHistory(calendar)
            sendMsg(data)
        }
    }

    fun notifyMetHistory(): String {
        val data = bleResponse.obj!!
        val status = data["status"] as Int
        if (status == 0) {
            var str = ""
            if (data.containsKey("met_list")) {
                str = ("Sampling interval: " + data["interval"] + " minutes\n"
                        + "Date: " + data["year"] + "-" + data["month"] + "-" + data["day"] + "\n"
                        + "Data length: " + data["data_length"] + "\n"
                        )

                val dataList = data["met_list"] as ArrayList<*>
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i] as HashMap<*, *>
                    str += " MET: " + oneData["met"] + "\n"
                }
            }

            return str
        } else {
            return statusLabel(status)
        }
    }


    fun getTempHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        changeMtu {
            val data = StarmaxSend().getTempHistory(calendar)
            sendMsg(data)
        }
    }

    fun notifyTempHistory(): String {
        val data = bleResponse.obj!!
        val status = data["status"] as Int
        if (status == 0) {
            var str = ("Sampling interval: " + data["interval"] + " minutes\n"
                    + "Date: " + data["year"] + "-" + data["month"] + "-" + data["day"] + "\n"
                    + "Data length: " + data["data_length"] + "\n"
                    )

            val dataList = data["temp_list"] as ArrayList<*>
            for (i in 0 until dataList.size) {
                val oneData = dataList[i] as HashMap<*, *>
                str += "Time: " + oneData["hour"] + ":" + oneData["minute"] + " Temperature: " + oneData["temp"] + "\n"
            }

            return str
        } else {
            return statusLabel(status)
        }
    }


    fun getMaiHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        changeMtu {
            val data = StarmaxSend().getMai(calendar)
            sendMsg(data)
        }
    }

    fun notifyMaiHistory(): String {
        val data = bleResponse.obj!!
        val status = data["status"] as Int
        if (status == 0) {
            var str = ("Sampling Interval:" + data["interval"] + " minutes\n"
                    + "Date:" + data["year"] + "-" + data["month"] + "-" + data["day"] + "\n"
                    + "Data Length:" + data["data_length"] + "\n"
                    )

            val dataList = data["mai_list"] as ArrayList<*>
            for (i in 0 until dataList.size) {
                val oneData = dataList[i] as HashMap<*, *>
                str += " Mai Data:" + oneData["mai"] + "\n"
            }

            return str
        } else {
            return statusLabel(status)
        }
    }


    fun getBloodSugarHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        changeMtu {
            val data = StarmaxSend().getBloodSugarHistory(calendar)
            sendMsg(data)
        }
    }

    fun notifyBloodSugarHistory(): String {
        val data = bleResponse.obj!!
        val status = data["status"] as Int
        if (status == 0) {
            var str = ("ระยะเวลาการเก็บข้อมูลตัวอย่าง:" + data["interval"] + "นาที\n"
                    + "วันที่:" + data["year"] + "-" + data["month"] + "-" + data["day"] + "\n"
                    + "ความยาวของข้อมูล:" + data["data_length"] + "\n"
                    )

            val dataList = data["blood_sugar_list"] as ArrayList<*>
            for (i in 0 until dataList.size) {
                val oneData = dataList[i] as HashMap<*, *>
                str += "เวลา:" + oneData["hour"] + ":" + oneData["minute"] + " ระดับน้ำตาลในเลือด" + oneData["blood_sugar"] + "\n"
            }

            return str
        } else {
            return statusLabel(status)
        }
    }


    fun getValidHistoryDates() {
        val data = StarmaxSend().getValidHistoryDates(HistoryType.Step)
        sendMsg(data)
    }

    fun getMetValidHistoryDates() {
        val data = StarmaxSend().getValidHistoryDates(HistoryType.Met)
        sendMsg(data)
    }

    fun getMaiValidHistoryDates() {
        val data = StarmaxSend().getValidHistoryDates(HistoryType.Mai)
        sendMsg(data)
    }

    fun getBloodSugarValidHistoryDates() {
        val data = StarmaxSend().getValidHistoryDates(HistoryType.BloodSugar)
        sendMsg(data)
    }

    fun notifyValidHistoryDates(): String {
        val data = bleResponse.obj!!
        val status = data["status"] as Int
        if (status == 0) {
            var str = "Valid Dates\n"

            val dataList = data["valid_history_dates"] as ArrayList<*>
            for (i in 0 until dataList.size) {
                val oneData = dataList[i] as HashMap<*, *>
                val year = oneData["year"]
                val month = oneData["month"]
                val day = oneData["day"]
                str += "$year-$month-$day\n"
            }

            return str
        } else {
            return statusLabel(status)
        }
    }


    fun notifyDialInfo(): String {
        val data = bleResponse.obj!!
        val status = data["status"] as Int
        if (status == 0) {
            var str = ""

            val dataList = data["dial_list"] as ArrayList<*>
            for (i in 0 until dataList.size) {
                val oneData = dataList[i] as HashMap<*, *>
                val isSelected = oneData["is_selected"]
                val dialId = oneData["dial_id"]
                val dialColor = oneData["dial_color"] as Int
                val align = oneData["align"]
                if (isSelected == 1) {
                    str += "เลือกแล้ว\n"
                }
                str += "ไอดีหน้าปัด:${dialId}\n"
                str += "สีของหน้าปัด:${
                    Utils.bytesToHex(
                        Utils.int2byte(
                            dialColor,
                            3
                        )
                    )
                }\n"
                str += "ตำแหน่ง:${align}\n"
            }

            return str
        } else {
            return statusLabel(status)
        }
    }

    fun sendUi() {
        object : Thread() {
            override fun run() {
                UiRepository.getVersion(model = bleModel, version = bleUiVersion, onSuccess = { ui, _ ->
                    if (ui == null) {
                        return@getVersion
                    }

                    val file = File(savePath)
                    if (!file.exists()) file.mkdirs()
                    val url = ui.binUrl
                    val saveName = url.substring(url.lastIndexOf('/') + 1, url.length)

                    val apkFile = File(savePath + saveName)
                    if (apkFile.exists()) apkFile.delete()
                    object : Thread() {
                        override fun run() {
                            try {
                                NetFileUtils.downloadUpdateFile(url, apkFile) {
                                    changeMtu {
                                        try {
                                            val fis = FileInputStream(apkFile)
                                            BleFileSender.initFile(fis,
                                                object : BleFileSenderListener() {
                                                    override fun onSuccess() {}

                                                    override fun onProgress(progress: Double) {
                                                        bleMessage.value = "Current Progress ${progress}%"
                                                    }

                                                    override fun onFailure() {
                                                        bleMessage.value = "Installation Failed"
                                                    }

                                                    override fun onStart() {
                                                        val data = StarmaxSend().sendUi(offset = 0, ui.version)
                                                        sendMsg(data)
                                                    }

                                                    override fun onSend() {
                                                        if (BleFileSender.hasNext()) {
                                                            val data = StarmaxSend().sendFile()
                                                            sendMsg(data)
                                                        }
                                                    }
                                                })

                                            BleFileSender.sliceBuffer = 8

                                            BleFileSender.onStart()
                                        } catch (e: FileNotFoundException) {
                                            bleMessage.value = "File Not Found"
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            } catch (e: java.lang.Exception) {
                                bleMessage.value = "Server Error"
                                e.printStackTrace()
                            }
                        }
                    }.start()
                }, onError = { e ->
                    bleMessage.value = "Server Error"
                    e?.printStackTrace()
                })
            }
        }.start()
    }

    fun sendUiDiff() {
        if (!uiSupportDifferentialUpgrade) {
            bleMessage.value = "Current Device Doesn't Support UI Differential Upgrade"
            return
        }
        object : Thread() {
            override fun run() {
                UiRepository.getDiff(model = bleModel, version = bleUiVersion, onSuccess = { ui, _ ->
                    if (ui == null) {
                        return@getDiff
                    }

                    val file = File(savePath)
                    if (!file.exists()) file.mkdirs()
                    val url = ui.binUrl
                    val saveName = url.substring(url.lastIndexOf('/') + 1, url.length)

                    val apkFile = File(savePath + saveName)
                    if (apkFile.exists()) apkFile.delete()
                    object : Thread() {
                        override fun run() {
                            try {
                                NetFileUtils.downloadUpdateFile(url, apkFile) {
                                    changeMtu {
                                        try {
                                            val fis = FileInputStream(apkFile)

                                            BleFileSender.initFile(fis,
                                                object : BleFileSenderListener() {
                                                    override fun onSuccess() {}

                                                    override fun onProgress(progress: Double) {
                                                        bleMessage.value = "Current Progress ${progress}%"
                                                    }

                                                    override fun onFailure() {}

                                                    override fun onStart() {
                                                        val data = StarmaxSend().sendUi(offset = ui.offset, ui.version)
                                                        sendMsg(data)
                                                    }

                                                    override fun onSend() {
                                                        if (BleFileSender.hasNext()) {
                                                            val data = StarmaxSend().sendFile()
                                                            sendMsg(data)
                                                        }
                                                    }
                                                })

                                            BleFileSender.sliceBuffer = 8

                                            BleFileSender.onStart()
                                        } catch (e: FileNotFoundException) {
                                            bleMessage.value = "File Not Found"
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            } catch (e: java.lang.Exception) {
                                bleMessage.value = "Server Error"
                                e.printStackTrace()
                            }
                        }
                    }.start()
                }, onError = { e ->
                    bleMessage.value = "Server Error"
                    e?.printStackTrace()
                })
            }
        }.start()
    }

    fun sendUiLocal(context: Context, uri: Uri) {
        try {
            val fis = context.contentResolver.openInputStream(uri)

            BleFileSender.initFile(fis,
                object :
                    BleFileSenderListener() {
                    override fun onSuccess() {}

                    override fun onProgress(progress: Double) {
                        bleMessage.value = "Current Progress ${progress}%"
                    }

                    override fun onFailure() {}

                    override fun onStart() {
                        val data = StarmaxSend()
                            .sendUi(offset = 0, "1.0.0")
                        sendMsg(data)
                    }

                    override fun onSend() {
                        if (BleFileSender.hasNext()) {
                            val data = StarmaxSend().sendFile()
                            sendMsg(data)
                        }
                    }
                })

            BleFileSender.sliceBuffer = 8

            BleFileSender.onStart()
        } catch (e: FileNotFoundException) {
            bleMessage.value = "File Not Found"
            e.printStackTrace()
        }
    }

    fun sendDialLocal(context: Context) {
        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                BleFileSender.initFile(
                    bin,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {}

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "Current Progress ${progress.toInt()}%"
                        }

                        override fun onFailure() {}

                        override fun onStart() {
                            val data = StarmaxSend()
                                .sendDial(
                                    30000,
                                    BmpUtils.bmp24to16(255, 255, 255),
                                    1
                                )
                            sendMsg(data)
                        }

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()
                                sendMsg(data)
                            }
                        }
                    })

                BleFileSender.sliceBuffer = 8

                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "Server Error"
                e.printStackTrace()
            }
        }
    }


    fun getDialInfo() {
        val data = StarmaxSend().getDialInfo()
        sendMsg(data)
    }

    fun switchDial() {
        val data = StarmaxSend().switchDial(5001)
        sendMsg(data)
    }

    fun reset() {
        val data = StarmaxSend().reset()
        sendMsg(data)
    }

    fun close() {
        val data = StarmaxSend().close()
        sendMsg(data)
    }

    /**
     * @param data
     */
    fun sendMsg(data: ByteArray?) {
        BleManager.getInstance().write(
            bleDevice?.get(),
            WriteServiceUUID.toString(),
            WriteCharacteristicUUID.toString(),
            data,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    //bleMessage.value = "指令发送成功"
                    //println("当前 $current 总共 $total 已写 $justWrite")
                }

                override fun onWriteFailure(exception: BleException?) {
                    //bleMessage.value = "指令发送失败"
                }
            })
    }

    fun changeMtu(onMtuChanged: () -> Unit) {
        BleManager.getInstance().setMtu(bleDevice?.get(), 512, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {
                // ตั้งMTUล้มเหลว
            }

            override fun onMtuChanged(mtu: Int) {
                BleManager.getInstance().setSplitWriteNum(mtu - 3)
                Log.e("BleViewModel", "ตั้งmtuสำเร็จ")
                onMtuChanged()
            }
        })
    }

    private fun statusLabel(status: Int): String {
        return when (status) {
            0 -> "คำสั่งถูกต้อง"
            1 -> "รหัสคำสั่งผิด"
            2 -> "รหัสตรวจสอบผิด"
            3 -> "ความยาวข้อมูลผิด"
            4 -> "ข้อมูลไม่ถูกต้อง"
            else -> "ข้อมูลไม่ถูกต้อง"
        };
    }

    private fun sportModeLabel(mode: Int): String {
        return when (mode) {
            0X00 -> "วิ่งในร่ม"
            0X01 -> "วิ่งนอกประเทศ"
            0X03 -> "ปั่นจักรยานนอกประเทศ"
            0X04 -> "เดิน"
            0X05 -> "กระโดดเชือก"
            0X06 -> "ฟุตบอล"
            0X07 -> "แบดมินตัน"
            0X09 -> "บาสเกตบอล"
            0X0A -> "เครื่องวิ่งราง"
            0X0B -> "เดินเท้า"
            0X0C -> "โยคะ"
            0X0D -> "ฝึกซ้อมกาย"
            0X0E -> "ปีนเขา"
            0X0F -> "กีฬาอิสระ"
            0X10 -> "เดินนอกบ้าน"
            0X12 -> "จักรยานอินโดร"
            else -> "ข้อมูลไม่ถูกต้อง"
        };
    }

}
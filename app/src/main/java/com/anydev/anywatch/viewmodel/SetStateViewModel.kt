

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.starmax.bluetoothsdk.StarmaxMapResponse
import com.starmax.bluetoothsdk.data.NotifyType
import org.json.JSONObject

class SetStateViewModel(

) : ViewModel() {
    var timeFormat by mutableStateOf(0)
    var unitFormat by mutableStateOf(0)
    var tempFormat by mutableStateOf(0)
    var language by mutableStateOf(1)
    var backlighting by mutableStateOf(5)
    var screen by mutableStateOf(0)
    var wristUp by mutableStateOf(false)

    fun setState(response: StarmaxMapResponse){
        if(response.type == NotifyType.GetState){
            val result = response.obj!!
            timeFormat = result["time_format"] as Int
            unitFormat = result["unit_format"] as Int
            tempFormat = result["temp_format"] as Int
            language = result["language"] as Int
            backlighting = result["backlighting"] as Int
            screen = result["screen"] as Int
            wristUp = result["wrist_up"] as Boolean
        }
    }
}
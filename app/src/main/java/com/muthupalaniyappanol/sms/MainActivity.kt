package com.muthupalaniyappanol.sms

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.view.RoundedCorner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.muthupalaniyappanol.sms.ui.theme.SMSTheme
import io.paperdb.Paper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var readSmsPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var sendSmsPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var receiveSmsPermissionLauncher: ActivityResultLauncher<String>

    private val READ_SMS_PERMISSION = 101
    private val SEND_SMS_PERMISSION = 102

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Paper.init(applicationContext)
        setContent {
            SMSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Scaffold(
                            topBar = {
                                 Text(text = "Automated SMS App", fontSize = 25.sp, textAlign = TextAlign.Center, modifier = Modifier
                                     .fillMaxWidth()
                                     .padding(10.dp))
                            },
                            content = {
                                Column(modifier = Modifier.padding(it)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextField(value = MainViewModel.phoneNumber.value, onValueChange = {
                                            MainViewModel.phoneNumber.value = it
                                            Paper.book().write("phoneNumber", it)
                                        }, modifier = Modifier.fillMaxWidth().padding(10.dp))
                                    }
                                    Messages(modifier = Modifier.padding(top = 10.dp))
                                }
                            }
                        )
                    }
                }
            }
        }

        setup()
        checkAndRequestPermissions()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setup() {
        readSmsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(applicationContext, "READ SMS IS NEEDED", Toast.LENGTH_SHORT).show()
            }
        }

        sendSmsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(applicationContext, "WRITE SMS IS NEEDED", Toast.LENGTH_SHORT).show()
            }
        }

        receiveSmsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(applicationContext, "RECEIVE SMS IS NEEDED", Toast.LENGTH_SHORT).show()
            }
        }

        Mqtt.init(applicationContext)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                Mqtt.connect(applicationContext)
            }
        }

        MainViewModel.phoneNumber.value = Paper.book().read<String>("phoneNumber") ?: "+918925423535"
        Paper.book().write("phoneNumber", MainViewModel.phoneNumber.value)

        registerReceivers()
    }

    fun registerReceivers() {
    }

    private fun unRegisterReceivers() {
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) !=
            PackageManager.PERMISSION_GRANTED) {
            readSmsPermissionLauncher.launch(android.Manifest.permission.READ_SMS)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED) {
            sendSmsPermissionLauncher.launch(android.Manifest.permission.SEND_SMS)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) !=
            PackageManager.PERMISSION_GRANTED) {
            sendSmsPermissionLauncher.launch(android.Manifest.permission.RECEIVE_SMS)
        }
    }

    override fun onStop() {
        super.onStop()
        unRegisterReceivers()
    }
}

object MainViewModel {
    val messages = mutableStateOf(listOf<MessageData>())
    val phoneNumber = mutableStateOf("")
}

data class MessageData (val from :String, val to:String, val msg:String)

@Composable
fun Message(msg :MessageData, modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .padding(10.dp)
        .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp))
        .padding(10.dp)
        .fillMaxWidth()
    ) {
        Text(text = "${msg.to} => ${msg.from}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(text = msg.msg, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun Messages(modifier: Modifier = Modifier){
    LazyColumn(
        modifier = modifier
    ) {
        items(MainViewModel.messages.value.size) {
            Message(msg=MainViewModel.messages.value[it])
        }
    }
}
package com.muthupalaniyappanol.sms

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

object Mqtt {
    @SuppressLint("StaticFieldLeak")
    lateinit var client : MqttAndroidClient

    fun init(applicationContext: Context) {
        client = MqttAndroidClient(applicationContext, "tcp://broker.emqx.io:1883", "MUTHUANDROID")
        client.setCallback(object :MqttCallback{
            override fun connectionLost(cause: Throwable?) {
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val data = Gson().fromJson(String(message?.payload ?: byteArrayOf()), MessageData::class.java)
                println(data.from.trim())
                println(MainViewModel.phoneNumber.value.trim())
                println(data.from.trim() == MainViewModel.phoneNumber.value.trim())
                if (data.from == MainViewModel.phoneNumber.value) {
                    try {
                        var smsManager = applicationContext.getSystemService(SmsManager::class.java)
                        if (smsManager == null) {
                            smsManager = SmsManager.getDefault();
                        }
                        println(smsManager)
                        smsManager.sendTextMessage(data.to, null, data.msg, null, null)
                        MainViewModel.messages.value = MainViewModel.messages.value.toMutableList().apply { add(data) }
                    } catch (e :Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }
        })
    }

    suspend fun connect(applicationContext: Context) {
        val options = MqttConnectOptions()
        client.connect(options, null, object: IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                    }
                    withContext(Dispatchers.IO) {
                        client.subscribe("smser", 0)
                    }
                }
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Failure", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
package com.muthupalaniyappanol.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.MqttMessage


class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        println(intent.action)
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in smsMessages) {
                val sender = sms.originatingAddress
                val messageBody = sms.messageBody
                val data = MessageData(from = sender ?: "", msg = messageBody, to = MainViewModel.phoneNumber.value)
                MainViewModel.messages.value = MainViewModel.messages.value.toMutableList().apply { add(data) }
                Mqtt.client.publish("smser", MqttMessage(Gson().toJson(data).toByteArray()))
            }
        }
    }
}
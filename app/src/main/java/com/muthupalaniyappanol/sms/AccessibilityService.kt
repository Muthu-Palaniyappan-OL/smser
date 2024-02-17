package com.muthupalaniyappanol.sms

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutomatorService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()

        println("I am executing")

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                Mqtt.init(applicationContext)
                Mqtt.connect(applicationContext)
            }
        }
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        println("I am executing Eve")
    }

    override fun onInterrupt() {
    }
}
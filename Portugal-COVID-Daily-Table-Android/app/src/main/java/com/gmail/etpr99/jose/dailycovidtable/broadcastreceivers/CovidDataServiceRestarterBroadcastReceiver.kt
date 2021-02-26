package com.gmail.etpr99.jose.dailycovidtable.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gmail.etpr99.jose.dailycovidtable.MainActivity
import com.gmail.etpr99.jose.dailycovidtable.listeners.CovidDataAvailableListenerService

class CovidDataServiceRestarterBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context!!.startService(Intent(context, CovidDataAvailableListenerService::class.java))
    }
}
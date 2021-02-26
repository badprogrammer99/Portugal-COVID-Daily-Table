package com.gmail.etpr99.jose.dailycovidtable.listeners

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.gmail.etpr99.jose.dailycovidtable.MainActivity
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PusherEvent
import com.pusher.client.channel.SubscriptionEventListener
import io.karn.notify.Notify
import io.karn.notify.Notify.Companion.CHANNEL_DEFAULT_KEY
import io.karn.notify.Notify.Companion.IMPORTANCE_HIGH

class CovidDataAvailableListenerService: Service(), SubscriptionEventListener {
    private lateinit var pusher: Pusher

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initPusherListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Intent("RestartService").also { intent -> sendBroadcast(intent) }
    }

    override fun onEvent(event: PusherEvent?) {
        val intentToMainActivity =  Intent(this@CovidDataAvailableListenerService, MainActivity::class.java)
        val notification = Notify.with(applicationContext)
            .alerting(CHANNEL_DEFAULT_KEY) {
                channelImportance = IMPORTANCE_HIGH
                lightColor = Color.RED
            }
            .meta {
                clickIntent = PendingIntent.getActivity(applicationContext,
                    0,
                    intentToMainActivity.putExtra("covidDataAvailable", true),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            .asBuilder()
            .setContentTitle("Tabela diária com dados da COVID-19 disponível")
            .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("A tabela com os dados da COVID-19 em Portugal do dia de hoje já se encontra disponível!"))
            .setContentText("A tabela com os dados da COVID-19 em Portugal do dia de hoje já se encontra disponível!")
            .build()

        with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
            notify(SystemClock.uptimeMillis().toInt(), notification)
        }
    }

    private fun initPusherListener() {
        pusher = Pusher(MainActivity.PUSHER_APP_KEY, PusherOptions().apply {
            setCluster(MainActivity.PUSHER_CLUSTER)
        }).apply {
            connect()
            subscribe(MainActivity.PUSHER_CHANNEL_NAME).bind(
                MainActivity.PUSHER_EVENT_NAME,
                this@CovidDataAvailableListenerService
            )
        }
    }
}
package com.gmail.etpr99.jose.dailycovidtable

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

import com.github.kittinunf.fuel.httpGet
import com.gmail.etpr99.jose.dailycovidtable.listeners.CovidDataAvailableListenerService
import com.gmail.etpr99.jose.dailycovidtable.listeners.CovidDataAvailableListenerService.CovidDataAvailableListenerBinder
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions

class MainActivity : AppCompatActivity() {
    companion object {
        const val PUSHER_APP_KEY = "98a6a8bb2fc2375f46b1"
        const val PUSHER_CLUSTER = "EU"
        const val PUSHER_CHANNEL_NAME = "energized-atoll-189"
        const val PUSHER_EVENT_NAME = "covid-data-available"

        const val HTML_COVID_TABLE_URL = "https://portugal-daily-covid-table.herokuapp.com/tables/html"
    }

    private lateinit var webView: WebView

    private lateinit var pusher: Pusher

    private lateinit var covidDataAvailableListenerServiceService: CovidDataAvailableListenerService
    private var isServiceBound = false

    private var connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as CovidDataAvailableListenerBinder
            covidDataAvailableListenerServiceService = binder.service
            isServiceBound = true
            initPusherListener()
            loadTableHtmlToWebView()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            covidDataAvailableListenerServiceService
            isServiceBound = false
            destroyPusherListener()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configureWebView()
        configurePusher()
        configureServiceBinding()
    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        val isCovidDataAvailable = intent.getBooleanExtra("covidDataAvailable", false)
        if (isCovidDataAvailable) loadTableHtmlToWebView()
    }

    private fun configureWebView() {
        webView = findViewById(R.id.covid_table_webview)
        webView.apply {
            settings.apply {
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = true
            }
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
        }
    }

    private fun configurePusher() {
        pusher = Pusher(PUSHER_APP_KEY, PusherOptions().apply { setCluster(PUSHER_CLUSTER) })
    }

    private fun configureServiceBinding() {
        Intent(this, CovidDataAvailableListenerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun initPusherListener() {
        pusher.apply {
            connect()
            subscribe(PUSHER_CHANNEL_NAME).bind(
                PUSHER_EVENT_NAME,
                covidDataAvailableListenerServiceService
            )
        }
    }

    private fun destroyPusherListener() {
        pusher.apply {
            unsubscribe(PUSHER_CHANNEL_NAME)
            disconnect()
        }
    }

    private fun loadTableHtmlToWebView() {
        HTML_COVID_TABLE_URL.httpGet().responseString { _, _, result ->
            runOnUiThread {
                webView.loadData(result.get(), "text/html;charset=utf-8", "UTF-8")
            }
        }
    }
}

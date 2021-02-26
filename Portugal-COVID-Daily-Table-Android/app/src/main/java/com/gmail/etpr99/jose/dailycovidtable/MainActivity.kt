package com.gmail.etpr99.jose.dailycovidtable

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

import com.github.kittinunf.fuel.httpGet
import com.gmail.etpr99.jose.dailycovidtable.listeners.CovidDataAvailableListenerService

class MainActivity : AppCompatActivity() {
    companion object {
        const val PUSHER_APP_KEY = "98a6a8bb2fc2375f46b1"
        const val PUSHER_CLUSTER = "EU"
        const val PUSHER_CHANNEL_NAME = "energized-atoll-189"
        const val PUSHER_EVENT_NAME = "covid-data-available"

        const val HTML_COVID_TABLE_URL = "https://portugal-daily-covid-table.herokuapp.com/tables/html"
    }

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configureWebView()
        loadTableHtmlToWebView()
        startService(Intent(this, CovidDataAvailableListenerService::class.java))
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

    private fun loadTableHtmlToWebView() {
        HTML_COVID_TABLE_URL.httpGet().responseString { _, _, result ->
            runOnUiThread {
                webView.loadData(result.get(), "text/html;charset=utf-8", "UTF-8")
            }
        }
    }
}
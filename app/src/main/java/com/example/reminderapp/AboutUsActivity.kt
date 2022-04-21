package com.example.reminderapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class AboutUsActivity : AppCompatActivity() {
    lateinit var myWebView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        myWebView = findViewById(R.id.myWebView)
        webViewSetup()

    }

    private fun webViewSetup() {

        myWebView.webViewClient = WebViewClient()
        myWebView.apply {
            loadUrl("https://www.google.com/")
            settings.javaScriptEnabled = true
            settings.safeBrowsingEnabled = true

        }
    }

    override fun onBackPressed() {
        if (myWebView.canGoBack()) myWebView.goBack() else super.onBackPressed()
    }
}
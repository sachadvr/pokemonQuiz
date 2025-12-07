package org.example.project

import android.content.Context

var androidContext: Context? = null

actual fun getApiBaseUrl(): String {
    return if (TestConfig.isTestMode()) {
        "http://127.0.0.1:8080"
    } else {
        "https://tyradex.vercel.app"
    }
}
